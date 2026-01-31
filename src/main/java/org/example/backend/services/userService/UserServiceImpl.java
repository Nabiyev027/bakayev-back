package org.example.backend.services.userService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.GroupStudentStatus;
import org.example.backend.Enum.PaymentStatus;
import org.example.backend.dto.*;
import org.example.backend.dtoResponse.*;
import org.example.backend.entity.*;
import org.example.backend.repository.*;
import org.example.backend.security.service.JwtService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final GroupRepo groupRepo;
    private final DiscountRepo discountRepo;
    private final FilialRepo filialRepo;
    private final GroupStudentRepo groupStudentRepo;
    private final PaymentRepo paymentRepo;
    private final TeacherSalaryRepo teacherSalaryRepo;
    private final ReceptionSalaryRepo receptionSalaryRepo;
    private final PaymentCourseInfoRepo paymentCourseInfoRepo;

    @Override
    @Transactional
    public Optional<User> register(
            String firstName,
            String lastName,
            String phone,
            String parentPhone,
            String username,
            String password,
            String groupId,
            String role,
            Integer discount,
            Integer discountTime,
            Integer teacherSalary,
            Integer receptionSalary,
            MultipartFile image,
            String filialId
    ) {

        // 1️⃣ Username tekshirish
        if (userRepo.existsByUsername(username)) {
            throw new RuntimeException("Bu username allaqachon mavjud!");
        }

        // 2️⃣ Role tekshirish
        Role roleEntity = roleRepo.findByName(role)
                .orElseThrow(() -> new RuntimeException("Role topilmadi"));

        // 3️⃣ Group majburiy emas
        Group group = null;
        if (groupId != null && !groupId.isBlank()) {
            try {
                UUID groupUUID = UUID.fromString(groupId);
                group = groupRepo.findById(groupUUID)
                        .orElseThrow(() -> new RuntimeException("Group topilmadi"));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Group ID noto‘g‘ri formatda");
            }
        }

        // 4️⃣ User yaratish
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(List.of(roleEntity));

        if (parentPhone != null && !parentPhone.isBlank()) {
            user.setParentPhone(parentPhone);
        }

        // 5️⃣ Filial biriktirish
        Filial filial = filialRepo.findById(UUID.fromString(filialId))
                .orElseThrow(() -> new RuntimeException("Filial topilmadi"));
        user.setFilials(List.of(filial));

        // 6️⃣ Image saqlash
        if (image != null && !image.isEmpty()) {
            user.setImageUrl(createImage(image));
        }

        // 7️⃣ Userni saqlash
        User savedUser = userRepo.save(user);

        // 8️⃣ Group bo‘lsa — bog‘lash
        if (group != null) {

            if ("ROLE_STUDENT".equals(roleEntity.getName())) {
                GroupStudent groupStudent = new GroupStudent();
                groupStudent.setGroup(group);
                groupStudent.setStudent(savedUser);
                groupStudent.setStatus(GroupStudentStatus.ACTIVE);
                groupStudentRepo.save(groupStudent);

            } else if ("ROLE_TEACHER".equals(roleEntity.getName())) {
                group.getTeachers().add(savedUser);
                groupRepo.save(group);
            }
        }

        // Teacher salary (group may be optional)
        if ("ROLE_TEACHER".equals(roleEntity.getName())) {
            TeacherSalary salary = new TeacherSalary();
            salary.setTeacher(savedUser);
            if (group != null) {
                salary.setGroup(group); // agar group tanlangan bo‘lsa
            }
            salary.setSalaryDate(LocalDate.now());
            teacherSalaryRepo.save(salary);
        }


        // 🔟 Reception salary
        if ("ROLE_RECEPTION".equals(roleEntity.getName())
                || "ROLE_MAIN_RECEPTION".equals(roleEntity.getName())) {
            ReceptionSalary salary = new ReceptionSalary();
            salary.setReceptionist(savedUser);
            salary.setSalaryAmount(receptionSalary);
            salary.setSalaryDate(LocalDate.now());
            receptionSalaryRepo.save(salary);
        }

        // 1️⃣1️⃣ Student discount
        if ("ROLE_STUDENT".equals(roleEntity.getName())
                && discount != null && discount > 0
                && discountTime != null && discountTime > 0) {

            Discount newDiscount = new Discount();
            newDiscount.setStudent(savedUser);
            newDiscount.setQuantity(discount);
            newDiscount.setEndDate(LocalDate.now().plusMonths(discountTime));
            discountRepo.save(newDiscount);
        }

        return Optional.of(savedUser);
    }



    @Override
    public List<TeacherNameDto> getTeachers() {
        List<Role> roles = new ArrayList<>();
        roleRepo.findByName("ROLE_TEACHER").ifPresent(roles::add);

        List<User> roleTeachers = userRepo.getByRoles(roles);
        List<TeacherNameDto> teacherNameDtos = new ArrayList<>();

        roleTeachers.forEach(teacher -> {
            // 🔹 STATUS = true bo‘lmaganlarni o'tkazib yuboramiz
            if (!teacher.isStatus()) return;

            TeacherNameDto teacherNameDto = new TeacherNameDto();
            teacherNameDto.setId(teacher.getId());
            teacherNameDto.setName(teacher.getFirstName() + " " + teacher.getLastName());
            teacherNameDtos.add(teacherNameDto);
        });

        return teacherNameDtos;
    }


    @Transactional
    @Override
    public List<StudentResDto> getStudentsWithData(String filialId, String groupId) {
        List<StudentResDto> students = new ArrayList<>();

        Optional<Role> roleOpt = roleRepo.findByName("ROLE_STUDENT");
        if (roleOpt.isEmpty()) {
            System.out.println("ROLE_STUDENT not found!");
            return students;
        }

        Role studentRole = roleOpt.get();
        List<User> roleStudents = userRepo.getByRoles(List.of(studentRole));

        System.out.println("Found students count: " + roleStudents.size());

        for (User s : roleStudents) {
            // Filial bo'yicha filter
            if (!"all".equals(filialId)) {
                boolean hasFilial = s.getFilials().stream()
                        .anyMatch(f -> f.getId().toString().equals(filialId));
                if (!hasFilial) continue;
            }

            // Group bo'yicha filter
            if (!"all".equals(groupId)) {
                boolean hasGroup = s.getGroupStudents().stream()
                        .anyMatch(gs -> gs.getGroup().getId().toString().equals(groupId));
                if (!hasGroup) continue;
            }


            StudentResDto newStudent = new StudentResDto();
            newStudent.setId(s.getId());
            newStudent.setImgUrl(s.getImageUrl());
            newStudent.setFirstName(s.getFirstName());
            newStudent.setLastName(s.getLastName());
            newStudent.setUsername(s.getUsername());
            newStudent.setPhone(s.getPhone());
            newStudent.setParentPhone(s.getParentPhone());

            // Filialni birinchi element sifatida olish
            if (s.getFilials() != null && !s.getFilials().isEmpty()) {
                Filial first = s.getFilials().get(0);
                FilialNameDto filialNameDto = new FilialNameDto();
                filialNameDto.setId(first.getId());
                filialNameDto.setName(first.getName());
                newStudent.setFilialNameDto(filialNameDto);
            }

            // Guruhlar
            List<GroupsNamesDto> groups = new ArrayList<>();
            if (s.getGroupStudents() != null) {
                for (var groupStudent : s.getGroupStudents()) {
                    GroupsNamesDto groupsNames = new GroupsNamesDto();
                    groupsNames.setId(groupStudent.getGroup().getId());
                    groupsNames.setName(groupStudent.getGroup().getName());
                    groups.add(groupsNames);
                }
            }

            newStudent.setGroups(groups);

            students.add(newStudent);
        }

        return students;
    }

//    @Transactional
//    @Override
//    public List<TeacherResDto> getTeachersWithData(String filialId) {
//        List<TeacherResDto> teachers = new ArrayList<>();
//
//        Optional<Role> roleOpt = roleRepo.findByName("ROLE_TEACHER");
//        if (roleOpt.isEmpty()) {
//            System.out.println("ROLE_TEACHER not found!");
//            return teachers;
//        }
//
//        Role teacherRole = roleOpt.get();
//        List<User> roleTeachers = userRepo.getByRoles(List.of(teacherRole));
//
//        System.out.println("Found students count: " + roleTeachers.size());
//
//        roleTeachers.forEach(t -> {
//            TeacherResDto newTeacher = new TeacherResDto();
//            newTeacher.setId(t.getId());
//            newTeacher.setImgUrl(t.getImageUrl());
//            newTeacher.setFirstName(t.getFirstName());
//            newTeacher.setLastName(t.getLastName());
//            newTeacher.setUsername(t.getUsername());
//            newTeacher.setPhone(t.getPhone());
//
//            List<FilialNameDto> filials = new ArrayList<>();
//            if (t.getFilials() != null) {
//                t.getFilials().forEach(filial -> {
//                    FilialNameDto filialNameDto = new FilialNameDto();
//                    filialNameDto.setId(filial.getId());
//                    filialNameDto.setName(filial.getName());
//                    filials.add(filialNameDto);
//                });
//            }
//
//            newTeacher.setBranches(filials);
//
//            List<GroupsNamesDto> groups = new ArrayList<>();
//            if (t.getTeacherGroups() != null) {
//                t.getTeacherGroups().forEach(group -> {
//                    GroupsNamesDto groupsNames = new GroupsNamesDto();
//                    groupsNames.setId(group.getId());
//                    groupsNames.setName(group.getName());
//                    groups.add(groupsNames);
//                });
//            }
//            newTeacher.setGroups(groups);
//
//            teachers.add(newTeacher);
//        });
//
//        return teachers;
//    }

    @Transactional
    @Override
    public List<TeacherResDto> getTeachersWithData(String filialId) {
        List<TeacherResDto> teachers = new ArrayList<>();

        // ROLE_TEACHER topilmasa bo'sh qaytamiz
        Role teacherRole = roleRepo.findByName("ROLE_TEACHER")
                .orElse(null);
        if (teacherRole == null) return teachers;

        // Teacher larni olish
        List<User> roleTeachers = userRepo.getByRoles(List.of(teacherRole));

        // Agar filialId = all bo'lsa filtrlamaymiz
        if (!filialId.equals("all")) {
            roleTeachers = roleTeachers.stream()
                    .filter(t -> t.getFilials() != null &&
                            t.getFilials().stream().anyMatch(f -> f.getId().toString().equals(filialId)))
                    .toList();
        }

        // Teacherlarni DTO ga o‘girish
        for (User t : roleTeachers) {
            TeacherResDto dto = new TeacherResDto();
            dto.setId(t.getId());
            dto.setImgUrl(t.getImageUrl());
            dto.setFirstName(t.getFirstName());
            dto.setLastName(t.getLastName());
            dto.setUsername(t.getUsername());
            dto.setPhone(t.getPhone());

            // Filiallar
            List<FilialNameDto> filials = new ArrayList<>();

            if (t.getFilials() != null && !t.getFilials().isEmpty()) {
                for (Filial f : t.getFilials()) {
                    FilialNameDto filialNameDto = new FilialNameDto();
                    filialNameDto.setId(f.getId());
                    filialNameDto.setName(f.getName());
                    filials.add(filialNameDto);
                }
            }

            dto.setBranches(filials);

            // Guruhlar
            List<GroupsNamesDto> groups = new ArrayList<>();
            if (t.getTeacherGroups() != null) {

                for (var group : t.getTeacherGroups()) {
                    GroupsNamesDto groupsNames = new GroupsNamesDto();
                    groupsNames.setId(group.getId());
                    groupsNames.setName(group.getName());
                    groups.add(groupsNames);
                }
            }
            dto.setGroups(groups);

            teachers.add(dto);
        }

        return teachers;
    }


    @Override
    public List<Role> getEmpRoles() {
        List<Role> roles = new ArrayList<>();
        roleRepo.findAll().forEach(role -> {
            String roleName = role.getName();
            if (roleName.equals("ROLE_RECEPTION") ||
                    roleName.equals("ROLE_TEACHER") ||
                    roleName.equals("ROLE_MAIN_RECEPTION") || roleName.equals("ROLE_ADMIN")) {
                roles.add(role);
            }
        });
        return roles;
    }

    @Transactional
    @Override
    public List<EmployerResDto> getEmployers(String filialId, String roleId) {
        List<EmployerResDto> employers = new ArrayList<>();

        userRepo.findAll().forEach(user -> {

            // 🔹 STATUS = true bo‘lmaganlarni o'tkazib yuboramiz
            if (!user.isStatus()) return;

            // 1) ROLE_STUDENT bo'lsa – SKIP
            if (user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_STUDENT"))) return;

            // 1.1) ROLE_SUPER_ADMIN bo'lsa – SKIP
            if (user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_SUPER_ADMIN"))) return;

            // 2) roleId = all emas bo'lsa filtr qilamiz
            if (!roleId.equals("all")) {
                boolean hasRole = user.getRoles().stream()
                        .anyMatch(r -> r.getId().toString().equals(roleId));
                if (!hasRole) return;
            }

            // 3) filialId = all emas bo'lsa filtr qilamiz
            if (!filialId.equals("all")) {
                boolean hasFilial = user.getFilials().stream()
                        .anyMatch(f -> f.getId().toString().equals(filialId));
                if (!hasFilial) return;
            }

            // DTO to'ldiramiz
            EmployerResDto employer = new EmployerResDto();
            employer.setId(user.getId());
            employer.setImgUrl(user.getImageUrl());
            employer.setFirstName(user.getFirstName());
            employer.setLastName(user.getLastName());
            employer.setUsername(user.getUsername());
            employer.setPhone(user.getPhone());
            employer.setRoles(user.getRoles());

            // Filial name dto lar
            List<FilialNameDto> filialNameDtos = new ArrayList<>();
            if (user.getFilials() != null) {
                user.getFilials().forEach(filial -> {
                    FilialNameDto dto = new FilialNameDto();
                    dto.setId(filial.getId());
                    dto.setName(filial.getName());
                    filialNameDtos.add(dto);
                });
            }
            employer.setFilialNameDtos(filialNameDtos);

            employers.add(employer);
        });

        return employers;
    }


    @Transactional
    @Override
    public void updateTeacher(UUID id, TeacherDto teacherDto) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Foydalanuvchi topilmadi: " + id));

        if (teacherDto.getFirstName() != null) user.setFirstName(teacherDto.getFirstName());
        if (teacherDto.getLastName() != null) user.setLastName(teacherDto.getLastName());
        if (teacherDto.getUsername() != null) user.setUsername(teacherDto.getUsername());
        if (teacherDto.getPhone() != null) user.setPhone(teacherDto.getPhone());

        // --- Filials ---
        if (teacherDto.getFilialIds() != null) {
            List<Filial> requestedFilials = teacherDto.getFilialIds().stream()
                    .map(fId -> filialRepo.findById(fId)
                            .orElseThrow(() -> new EntityNotFoundException("Filial not found: " + fId)))
                    .collect(Collectors.toList());

            // Defensive: eski filiallardan o'chirish (agar sizga kerak bo'lsa)
            List<Filial> oldFilials = Optional.ofNullable(user.getFilials()).orElse(Collections.emptyList());
            for (Filial old : new ArrayList<>(oldFilials)) {
                if (old.getUsers() != null) old.getUsers().remove(user);
            }

            // Yangi filialga qo'shish (bilateral update)
            for (Filial f : requestedFilials) {
                if (f.getUsers() == null) f.setUsers(new ArrayList<>());
                if (!f.getUsers().contains(user)) f.getUsers().add(user);
            }

            // Foydalanuvchi tomonini yangilash (agar User owning bo'lsa shu yetarli)
            user.setFilials(new ArrayList<>(requestedFilials));

            // Agar Filial owning bo'lsa yoki siz inverse tomonlarni ham saqlamoqchi bo'lsangiz:
            List<Filial> toSaveFilials = Stream.concat(oldFilials.stream(), requestedFilials.stream())
                    .distinct().collect(Collectors.toList());
            filialRepo.saveAll(toSaveFilials);
        }

        // --- Groups ---
        if (teacherDto.getGroupIds() != null) {
            List<Group> requestedGroups = teacherDto.getGroupIds().stream()
                    .map(gid -> groupRepo.findById(gid)
                            .orElseThrow(() -> new EntityNotFoundException("Group not found: " + gid)))
                    .collect(Collectors.toList());

            // Eski guruhlardan o'chirish — Eslatma: bu yerda getTeacherGroups() ishlatilsin
            List<Group> oldGroups = Optional.ofNullable(user.getTeacherGroups()).orElse(Collections.emptyList());
            for (Group old : new ArrayList<>(oldGroups)) {
                if (old.getTeachers() != null) old.getTeachers().remove(user);
            }

            // Yangi guruhlarga qo'shish (bilateral)
            for (Group g : requestedGroups) {
                if (g.getTeachers() == null) g.setTeachers(new ArrayList<>());
                if (!g.getTeachers().contains(user)) g.getTeachers().add(user);
            }

            user.setTeacherGroups(new ArrayList<>(requestedGroups));

            // Saqlash — eski va yangi guruhlarni saqlang, agar Group owning bo'lsa
            List<Group> toSaveGroups = Stream.concat(oldGroups.stream(), requestedGroups.stream())
                    .distinct().collect(Collectors.toList());
            groupRepo.saveAll(toSaveGroups);
        }

        // Oxirida user ni saqlaymiz
        userRepo.save(user);
    }

    @Transactional
    @Override
    public void updateEmployer(UUID id, EmployerDto employerDto) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Foydalanuvchi topilmadi: " + id));

        // --- Basic Fields ---
        if (employerDto.getFirstName() != null) user.setFirstName(employerDto.getFirstName());
        if (employerDto.getLastName() != null) user.setLastName(employerDto.getLastName());
        if (employerDto.getUsername() != null) user.setUsername(employerDto.getUsername());
        if (employerDto.getPhone() != null) user.setPhone(employerDto.getPhone());


        // ======================
        //      FILIALS UPDATE
        // ======================
        if (employerDto.getFilialIds() != null) {

            List<Filial> requestedFilials = employerDto.getFilialIds().stream()
                    .map(fid -> filialRepo.findById(fid)
                            .orElseThrow(() -> new EntityNotFoundException("Filial topilmadi: " + fid)))
                    .collect(Collectors.toList());

            // Eski filiallardan o'chirish
            List<Filial> oldFilials = Optional.ofNullable(user.getFilials()).orElse(Collections.emptyList());
            for (Filial old : new ArrayList<>(oldFilials)) {
                if (old.getUsers() != null) {
                    old.getUsers().remove(user);
                }
            }
            
            for (Filial f : requestedFilials) {
                if (f.getUsers() == null) f.setUsers(new ArrayList<>());
                if (!f.getUsers().contains(user)) f.getUsers().add(user);
            }

            user.setFilials(new ArrayList<>(requestedFilials));

            List<Filial> toSave = Stream.concat(oldFilials.stream(), requestedFilials.stream())
                    .distinct()
                    .collect(Collectors.toList());
            filialRepo.saveAll(toSave);
        }


        if (employerDto.getRoleIds() != null) {

            List<Role> newRoles = employerDto.getRoleIds().stream()
                    .map(rid -> roleRepo.findById(rid)
                            .orElseThrow(() -> new EntityNotFoundException("Role topilmadi: " + rid)))
                    .collect(Collectors.toList());

            user.setRoles(new ArrayList<>(newRoles));
        }

        userRepo.save(user);
    }


    @Transactional(readOnly = true)
    @Override
    public List<StudentForMessageResDto> getStudentForMessage(UUID filialId, UUID groupId) {
        List<StudentForMessageResDto> studentForMessageResDtos = new ArrayList<>();

        Filial filial = filialRepo.findById(filialId)
                .orElseThrow(() -> new EntityNotFoundException("Filial not found: " + filialId));

        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found: " + groupId));

        List<User> students = userRepo.findStudentsByFilialAndGroup(filial, group);

        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        LocalDate today = LocalDate.now();

        for (User student : students) {
            StudentForMessageResDto dto = new StudentForMessageResDto();
            dto.setId(student.getId());
            dto.setFirstName(student.getFirstName());
            dto.setLastName(student.getLastName());
            dto.setPhone(student.getPhone());
            dto.setParentPhone(student.getParentPhone());

            // 🔹 Debt hisoblash
            int debtAmount = 0;
            for (Debts d : student.getDebts()) {
                LocalDate debtDate = d.getCreatedDate();
                if (debtDate != null && (debtDate.isBefore(today) || debtDate.isEqual(today))) {
                    debtAmount += d.getAmount();
                }
            }
            dto.setDebt(debtAmount);

            // 🔹 Payment status hisoblash (joriy oy uchun)
            List<Payment> paymentsByStudent = paymentRepo.findPaymentsByStudent(student);
            dto.setPaid(PaymentStatus.NOTPAID.toString());
            for (Payment payment : paymentsByStudent) {
                LocalDate payDate = payment.getDate();
                if (payDate.getYear() == currentYear && payDate.getMonthValue() == currentMonth) {
                    dto.setPaid(payment.getPaymentStatus().toString());
                    break; // shu oy topildi, tekshirishni tugatamiz
                }
            }

            studentForMessageResDtos.add(dto);
        }

        return studentForMessageResDtos;
    }


    @Override
    public List<StudentNameResDto> getStudentsByGroup(UUID groupId) {
        List<StudentNameResDto> students = new ArrayList<>();

        Group group = groupRepo.findById(groupId).orElseThrow(() -> new EntityNotFoundException("Group not found: " + groupId));

        if(group!=null){
            List<User> studentsByGroup = userRepo.findStudentsByGroup(group);
            studentsByGroup.forEach(student -> {
                StudentNameResDto studentNameResDto = new StudentNameResDto();
                studentNameResDto.setId(student.getId());
                studentNameResDto.setName(student.getFirstName() + " " + student.getLastName());
                students.add(studentNameResDto);
            });
        }

        return students;
    }

    @Override
    public void changeEmployerPassword(UUID userId, String newPassword) {
        User user = userRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        if(user!=null){
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepo.save(user);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<TeacherNameDto> getTeachersByFilial(UUID filialId) {
        Filial filial = filialRepo.findById(filialId)
                .orElseThrow(() -> new RuntimeException("Filial topilmadi"));

        List<TeacherNameDto> teachers = new ArrayList<>();

        filial.getUsers().forEach(user -> {
            // 🔹 STATUS = true bo‘lmaganlarni o'tkazib yuboramiz
            if (!user.isStatus()) return;

            boolean isTeacher = user.getRoles().stream()
                    .anyMatch(role -> "ROLE_TEACHER".equals(role.getName()));

            if (isTeacher) {
                TeacherNameDto dto = new TeacherNameDto();
                dto.setId(user.getId());
                dto.setName(user.getFirstName() + " " + user.getLastName());
                teachers.add(dto);
            }
        });

        return teachers;
    }


    @Transactional
    @Override
    public List<AdminResDto> getAdminsWithData() {
        List<AdminResDto> admins = new ArrayList<>();

        // ROLE_TEACHER topilmasa bo'sh qaytamiz
        Role adminRole = roleRepo.findByName("ROLE_ADMIN")
                .orElse(null);
        if (adminRole == null) return admins;

        // Teacher larni olish
        List<User> roleAdmin = userRepo.getByRoles(List.of(adminRole));

        // Teacherlarni DTO ga o‘girish
        for (User a : roleAdmin) {
            AdminResDto dto = new AdminResDto();
            dto.setId(a.getId());
            dto.setImgUrl(a.getImageUrl());
            dto.setFirstName(a.getFirstName());
            dto.setLastName(a.getLastName());
            dto.setUsername(a.getUsername());

            admins.add(dto);
        }

        return admins;
    }

    @Override
    public void registerForSuperAdmin(String firstName, String lastName, String username, String password) {

        if (userRepo.existsByUsername(username)) {
            throw new RuntimeException("Bu username allaqachon mavjud!");
        }

        // 1. Role borligini tekshirish
        Role roleAdmin = roleRepo.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        // 2. Username unikal bo‘lishi kerak
        if (userRepo.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        // 3. Password validatsiya
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password cannot be empty");
        }

        // 4. Foydalanuvchini yaratish
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(List.of(roleAdmin));

        userRepo.save(user);
    }

    @Transactional
    @Override
    public void updateForSuperAdmin(UUID id, String firstName, String lastName, String username, String password) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));

        // Username unique tekshirish (faqat o‘zgargan bo‘lsa)
        if (!user.getUsername().equals(username) && userRepo.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);

        // Password optional, faqat bo‘sh bo‘lmasa yangilaymiz
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        userRepo.save(user);
    }


    @Override
    public UserInfoResDto getUserInfo(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserInfoResDto dto = new UserInfoResDto();
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername());
        dto.setImgUrl(user.getImageUrl());

        return dto;
    }

    @Override
    public void updateUserInfo(UUID id, String firstName, String lastName, String username, MultipartFile img) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1️⃣ User ma'lumotlarini yangilash
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);

        // 2️⃣ Agar yangi rasm yuborilgan bo‘lsa
        if (img != null && !img.isEmpty()) {
            String newImgUrl;
            if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                // Eski rasmni o‘chirish va yangi rasmni yaratish
                newImgUrl = replaceImage(user.getImageUrl(), img);
            } else {
                // Yangi rasm yaratish
                newImgUrl = createImage(img);
            }
            user.setImageUrl(newImgUrl);
        }

        // 3️⃣ Saqlash
        userRepo.save(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<StudentInfoResDto> getStudentInfos(UUID groupId) {

        List<StudentInfoResDto> infos = new ArrayList<>();

        // GroupStudent dan status bilan birga olish
        List<GroupStudent> groupStudents = groupStudentRepo.findByGroup(groupId);

        for (GroupStudent gs : groupStudents) {

            User student = gs.getStudent();

            // 🔹 Faqat status = true bo'lgan userlarni olish
            if (!student.isStatus()) continue;

            StudentInfoResDto dto = new StudentInfoResDto();
            dto.setId(student.getId());
            dto.setName(student.getFirstName() + " " + student.getLastName());

            List<Discount> discountsByStudent = discountRepo.getDiscountsByStudent(student);

            discountsByStudent.forEach(discount -> {
                if(discount.getStudent().getId().equals(student.getId())) {
                    dto.setDiscount(discount.getQuantity());
                    dto.setEndDate(discount.getEndDate().toString());
                }
            });

            dto.setStatus(gs.getStatus().name());

            List<Payment> paymentsByStudent = paymentRepo.findPaymentsByStudent(student);

            dto.setPaymentStatus(PaymentStatus.NOTPAID.toString());

            int currentMonth = LocalDate.now().getMonthValue();
            int currentYear = LocalDate.now().getYear();

            for (Payment payment : paymentsByStudent) {
                LocalDate payDate = payment.getDate(); // allaqachon LocalDate

                if (payDate.getYear() == currentYear && payDate.getMonthValue() == currentMonth) {
                    dto.setPaymentStatus(payment.getPaymentStatus().toString());
                    break; // shu oy topildi, tekshirishni tugatamiz
                }
            }

            int debtAmount = 0;
            LocalDate today = LocalDate.now();
            for (Debts d : student.getDebts()) {
                LocalDate debtDate = d.getCreatedDate();
                if (debtDate != null && (debtDate.isBefore(today) || debtDate.isEqual(today))) {
                    debtAmount += d.getAmount();
                }
            }

            dto.setDebt(debtAmount);
            infos.add(dto);
        }

        return infos;
    }


    @Transactional
    @Override
    public void updateStatus(UUID id, String status, UUID groupId) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Faqat kerakli groupStudentni topamiz
        user.getGroupStudents().stream()
                .filter(gs -> gs.getGroup().getId().equals(groupId))
                .findFirst() // faqat bitta topamiz
                .ifPresent(gs -> {
                    gs.setStatus(GroupStudentStatus.valueOf(status)); // enumga mos kelishini tekshiring
                    groupStudentRepo.save(gs); // faqat bitta save
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountResDto> getStudentDiscounts(UUID id) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<DiscountResDto> discounts = new ArrayList<>();

        LocalDate today = LocalDate.now();

        discountRepo.getDiscountsByStudent(user).forEach(discount -> {
            DiscountResDto dto = new DiscountResDto();
            dto.setId(discount.getId());
            dto.setAmount(discount.getQuantity());
            dto.setEndDate(discount.getEndDate().toString());
            dto.setActive(discount.isActive());

            discounts.add(dto);
        });

        return discounts;
    }

    @Override
    @Transactional
    public void addDiscount(UUID userId, DiscountDto discountDto) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();

        Discount disc = discountRepo.findTopByStudentAndActiveOrderByEndDateDesc(user, true);

        if (disc != null) {
            disc.setActive(false); // eski discountni yopish kerak
            disc.setEndDate(today);
            discountRepo.save(disc);
        }

        Discount newDiscount = new Discount();
        newDiscount.setStudent(user);
        newDiscount.setQuantity(discountDto.getAmount());
        newDiscount.setActive(true);
        LocalDate endDate = today.plusMonths(Long.parseLong(discountDto.getLimitMonth()));
        newDiscount.setEndDate(endDate);

        user.getDiscounts().add(newDiscount);

        userRepo.save(user);
    }



    @Override
    @Transactional
    public void editDiscount(UUID discountId, DiscountDto discountDto) {
        // Mavjud discountni topamiz
        Discount discount = discountRepo.findById(discountId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));

        // Chegirma miqdorini yangilash
        discount.setQuantity(discountDto.getAmount());

        // End date ni yangilash (hozirgi sanadan + limitMonth)
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusMonths(Long.parseLong(discountDto.getLimitMonth()));
        discount.setEndDate(endDate);

        // Yangilangan chegirmani saqlash
        discountRepo.save(discount);
    }

    @Transactional
    @Override
    public void deleteDiscount(UUID id) {
        Discount discount = discountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found"));

        discountRepo.delete(discount);
    }


    @Transactional
    @Override
    public void updateStudent(UUID id, StudentDto studentDto) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("Foydalanuvchi topilmadi: " + id);
        }

        User user = optionalUser.get();


        if (studentDto.getFirstName() != null) {
            user.setFirstName(studentDto.getFirstName());
        }

        if (studentDto.getLastName() != null) {
            user.setLastName(studentDto.getLastName());
        }

        if (studentDto.getUsername() != null) {
            user.setUsername(studentDto.getUsername());
        }

        if (studentDto.getPhone() != null) {
            user.setPhone(studentDto.getPhone());
        }

        if (studentDto.getParentPhone() != null) {
            user.setParentPhone(studentDto.getParentPhone());
        }

        // Filial yangilanishi
        if (studentDto.getFilialId() != null) {
            Filial filial = filialRepo.findById(studentDto.getFilialId())
                    .orElseThrow(() -> new RuntimeException("Filial topilmadi: " + studentDto.getFilialId()));
            List<Filial> filials = new ArrayList<>();
            Filial filial1 = new Filial();
            filial1.setId(filial.getId());
            filial1.setName(filial.getName());
            filials.add(filial1);
            user.setFilials(filials);
        }

        if (studentDto.getGroupIds() != null) {
            List<Group> requestedGroups = studentDto.getGroupIds().stream()
                    .map(gid -> groupRepo.findById(gid)
                            .orElseThrow(() -> new EntityNotFoundException("Group not found: " + gid)))
                    .toList();

            // Eski guruhlarni olib tashlash
            for (GroupStudent gs : user.getGroupStudents()) {
                groupStudentRepo.delete(gs);  // DBdan ham o'chiramiz
            }
            user.getGroupStudents().clear(); // xotirada ham tozalaymiz

// Yangi guruhlarga qo‘shish
            for (Group g : requestedGroups) {
                GroupStudent gs = new GroupStudent();
                gs.setGroup(g);
                gs.setStudent(user);
                gs.setStatus(GroupStudentStatus.ACTIVE);
                groupStudentRepo.save(gs);

                user.getGroupStudents().add(gs); // xotirada sinxronlash uchun
            }

            // E’tibor: O‘zgargan guruhlarni saqlab qo‘yamiz
            groupRepo.saveAll(requestedGroups);
        }

        // Foydalanuvchini saqlash
        userRepo.save(user);
    }

    @Override
    public List<Role> getRoles() {
        List<Role> all = roleRepo.findAll();
        return all;
    }



    @Override
    public User getUserByUsername(String username){
        Optional<User> byPhone = userRepo.findByUsername(username);
        return byPhone.orElseThrow();
    }

    @Override
    public Map<String, String> login(LoginDto loginDto) {
        // 1️⃣ Foydalanuvchini username orqali olish
        User user = userRepo.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new RuntimeException("Foydalanuvchi topilmadi"));

        // 2️⃣ STATUS = true bo‘lmasa loginni rad etish
        if (!user.isStatus()) {
            throw new RuntimeException("Foydalanuvchi faol emas");
        }

        // 3️⃣ Authentication
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsername(),
                        loginDto.getPassword()
                )
        );

        // 4️⃣ JWT va Refresh token generatsiya qilish
        String jwt = jwtService.generateJwt(user.getId().toString(), authenticate);
        String refreshJwt = jwtService.generateRefreshJwt(user.getId().toString(), authenticate);

        // 5️⃣ Token va rollarni qaytarish
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", jwt);
        tokens.put("refresh_token", refreshJwt);
        tokens.put("roles", user.getRoles().toString());

        return tokens;
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        if(user!=null) {
            user.setStatus(false);
            userRepo.save(user);
        }
    }

    @Override
    public void changeLoginPassword(UUID id, String oldPassword, String newPassword) {
        User user = userRepo.findById(id).orElseThrow();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    private String createImage(MultipartFile img) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads";
            File uploadsFolder = new File(uploadDir);

            if (!uploadsFolder.exists()) {
                uploadsFolder.mkdirs();
            }

            String uniqueFileName = UUID.randomUUID() + "_" + img.getOriginalFilename();
            File destination = new File(uploadsFolder, uniqueFileName);
            img.transferTo(destination);

            // Agar rasmlar frontend static fayllarida ko‘rsatilsa:
            return "/uploads/" + uniqueFileName;

        } catch (IOException e) {
            e.printStackTrace(); // Konsolda to‘liq xatoni ko‘rsatish uchun
            throw new RuntimeException("Rasmni saqlab bo‘lmadi: " + e.getMessage(), e);
        }

    }

    private String replaceImage(String oldImgUrl, MultipartFile newImg) {
        Optional.ofNullable(oldImgUrl)
                .filter(url -> !url.isEmpty())
                .map(url -> url.substring(url.lastIndexOf("/") + 1))
                .map(fileName -> Paths.get(System.getProperty("user.dir"), "uploads", fileName))
                .ifPresent(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        System.err.println("Eski rasmni o‘chirishda xatolik: " + e.getMessage());
                    }
                });

        return createImage(newImg);
    }


    private String createVideo(MultipartFile video) {
        try {
            // static/uploads/ papkasini olish
            File uploadsFolder = new ClassPathResource("static/uploads/").getFile();

            // Papka mavjud bo'lmasa yaratamiz
            if (!uploadsFolder.exists()) {
                uploadsFolder.mkdirs();
            }

            // Unikal nom
            String uniqueVideoName = UUID.randomUUID().toString() + "_" + video.getOriginalFilename();

            // Faylni joylash
            File destination = new File(uploadsFolder, uniqueVideoName);
            video.transferTo(destination);

            // Frontend uchun URL ni qaytaramiz
            return "/uploads/" + uniqueVideoName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store video: " + video.getOriginalFilename(), e);
        }

    }


}
