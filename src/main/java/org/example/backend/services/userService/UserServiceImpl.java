package org.example.backend.services.userService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.EmployerDto;
import org.example.backend.dto.LoginDto;
import org.example.backend.dto.StudentDto;
import org.example.backend.dto.TeacherDto;
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

    @Override
    @Transactional
    public Optional<User> register(String firstName, String lastName, String phone, String parentPhone,
                                   String username, String password, String groupId, String role,
                                   Integer discount, String discountTitle, MultipartFile image, String filialId) {
        // 1. Role topamiz
        Optional<Role> roleOpt = roleRepo.findByName(role);
        if (roleOpt.isEmpty()) {
            return Optional.empty(); // noto‘g‘ri role
        }

        // 2. Group ID null yoki bo‘sh emasligini tekshiramiz
        Optional<Group> groupOpt = Optional.empty();
        if (groupId != null && !groupId.trim().isEmpty()) {
            try {
                UUID groupUUID = UUID.fromString(groupId);
                groupOpt = groupRepo.findById(groupUUID);
                if (groupOpt.isEmpty()) {
                    return Optional.empty(); // noto‘g‘ri group id
                }
            } catch (Exception e) {
                return Optional.empty(); // UUID format noto‘g‘ri
            }
        }

        Role role1 = roleOpt.get();
        Group group = groupOpt.orElse(null); // bo‘lishi ham mumkin, bo‘lmasligi ham

        // 3. Yangi foydalanuvchini yaratamiz
        User userNew = new User();
        userNew.setFirstName(firstName);
        userNew.setLastName(lastName);
        userNew.setUsername(username);
        userNew.setPhone(phone);
        if(parentPhone != null && !parentPhone.trim().isEmpty()) {
            userNew.setParentPhone(parentPhone);
        }
        userNew.setPassword(passwordEncoder.encode(password));
        userNew.setRoles(List.of(role1));
        Filial filial = filialRepo.findById(UUID.fromString(filialId)).get();
        List<Filial> filialList = new ArrayList<>();
        filialList.add(filial);
        userNew.setFilials(filialList);

        // 4. Agar rasm bo‘lsa, saqlaymiz
        if (image != null && !image.isEmpty()) {
            String imgPath = createImage(image);
            userNew.setImageUrl(imgPath);
        }

        // 5. Userni bazaga saqlaymiz
        User savedUser = userRepo.save(userNew);

        // 6. Groupga qo‘shamiz (agar group mavjud bo‘lsa)
        if (group != null) {
            if ("ROLE_STUDENT".equals(role1.getName())) {
                group.getStudents().add(savedUser);
            } else if ("ROLE_TEACHER".equals(role1.getName())) {
                group.getTeachers().add(savedUser);
            }
            groupRepo.save(group);
        }


            // 7. Agar bu student va discount > 0 bo‘lsa, chegirma yozamiz
        if ("ROLE_STUDENT".equals(role1.getName()) && discount != null && discount > 0) {
            Discount newDisc = new Discount();
            newDisc.setQuantity(discount);
            newDisc.setTitle(discountTitle);
            newDisc.setStudent(savedUser);
            discountRepo.save(newDisc);
        }

        return Optional.of(savedUser);
    }



    @Override
    @Transactional
    public Optional<User> registerForAdmin(String firstName, String lastName, String phone, String username,
                                           String password, String filialId, String role, String groupId,
                                           MultipartFile image) {
        // 1. Role topamiz
        Optional<Role> roleOpt = roleRepo.findByName(role);
        if (roleOpt.isEmpty()) {
            return Optional.empty(); // noto‘g‘ri role
        }

        // 2. Group ID null yoki bo‘sh emasligini tekshiramiz
        Optional<Group> groupOpt = Optional.empty();
        if (groupId != null && !groupId.trim().isEmpty()) {
            try {
                UUID groupUUID = UUID.fromString(groupId);
                groupOpt = groupRepo.findById(groupUUID);
                if (groupOpt.isEmpty()) {
                    return Optional.empty(); // noto‘g‘ri group id
                }
            } catch (Exception e) {
                return Optional.empty(); // UUID format noto‘g‘ri
            }
        }

        Role roleEntity = roleOpt.get();
        Group group = groupOpt.orElse(null);

        // 3. Filialni tekshiramiz
        Optional<Filial> filialOpt = filialRepo.findById(UUID.fromString(filialId));
        if (filialOpt.isEmpty()) {
            return Optional.empty(); // noto‘g‘ri filial
        }

        // 4. Foydalanuvchini yaratamiz
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(List.of(roleEntity));
        List<Filial> filialList = new ArrayList<>();
        Filial filialEntity = filialOpt.get();
        filialList.add(filialEntity);
        user.setFilials(filialList);

        // 5. Rasm bo‘lsa saqlaymiz
        if (image != null && !image.isEmpty()) {
            String imagePath = createImage(image);
            user.setImageUrl(imagePath);
        }

        // 6. Userni saqlaymiz
        User savedUser = userRepo.save(user);

        // 7. Groupga qo‘shamiz (agar mavjud bo‘lsa)
        if (group != null) {
            group.getTeachers().add(savedUser);
            groupRepo.save(group);
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
                boolean hasGroup = s.getStudentGroups().stream()
                        .anyMatch(g -> g.getId().toString().equals(groupId));
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
            if (s.getStudentGroups() != null) {
                for (var group : s.getStudentGroups()) {
                    GroupsNamesDto groupsNames = new GroupsNamesDto();
                    groupsNames.setId(group.getId());
                    groupsNames.setName(group.getName());
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
                    roleName.equals("ROLE_MAIN_RECEPTION")) {
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

            // 1) ROLE_STUDENT bo'lsa – SKIP (qaytarmaymiz)
            if (user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_STUDENT"))) {
                return; // Continue
            } else if (user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"))) {
                return;
            }

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



    @Override
    public List<StudentForMessageResDto> getStudentForMessage(UUID filialId, UUID groupId) {
        List <StudentForMessageResDto> studentForMessageResDtos = new ArrayList<>();
        Filial filial = filialRepo.findById(filialId).get();
        Group group = groupRepo.findById(groupId).orElseThrow(() -> new EntityNotFoundException("Group not found: " + groupId));

        List<User> students = userRepo.findStudentsByFilialAndGroup(filial, group);

        students.forEach(student -> {
            StudentForMessageResDto studentForMessageResDto = new StudentForMessageResDto();
            studentForMessageResDto.setId(student.getId());
            studentForMessageResDto.setFirstName(student.getFirstName());
            studentForMessageResDto.setLastName(student.getLastName());
            studentForMessageResDto.setPhone(student.getPhone());
            studentForMessageResDto.setParentPhone(student.getParentPhone());
            studentForMessageResDtos.add(studentForMessageResDto);
        });

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
            boolean isTeacher = user.getRoles().stream()
                    .anyMatch(role -> "ROLE_TEACHER".equals(role.getName())); // to‘g‘rilangan joy!

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

            // Avval eski bog'lanishni tozalash
            for (Group old : user.getStudentGroups()) {
                old.getStudents().remove(user);
            }

            // Yangi guruhlarga qo‘shish
            for (Group g : requestedGroups) {
                g.getStudents().add(user);
            }

            // Faqat xotirada sinxron bo‘lishi uchun
            user.setStudentGroups(new ArrayList<>(requestedGroups));

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
    public Map<?, ?> login(LoginDto loginDto){
        User user = userRepo.findByUsername(loginDto.getUsername()).orElseThrow();
        UUID id = user.getId();
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsername(),
                        loginDto.getPassword()
                )
        );

        String jwt = jwtService.generateJwt(id.toString(),authenticate);
        String refreshJwt = jwtService.generateRefreshJwt(id.toString(),authenticate);
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", jwt);
        tokens.put("refresh_token", refreshJwt);
        tokens.put("roles", user.getRoles().toString());
        return tokens;

    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        // 1. Remove user from groups where he is a student
        for (Group group : user.getStudentGroups()) {
            group.getStudents().remove(user);
        }
        user.getStudentGroups().clear();

        // 2. Remove user from groups where he is a teacher
        for (Group group : user.getTeacherGroups()) {
            group.getTeachers().remove(user);
        }
        user.getTeacherGroups().clear();

        // 3. Remove user from teachers (agar bu user student bo‘lsa)
        for (User teacher : user.getTeachers()) {
            teacher.getStudents().remove(user);
        }
        user.getTeachers().clear();

        // 4. Remove user from students (agar bu user teacher bo‘lsa)
        for (User student : user.getStudents()) {
            student.getTeachers().remove(user);
        }
        user.getStudents().clear();

        // 5. ReferenceStatus tozalash
        if (user.getReferenceStatuses() != null) {
            user.getReferenceStatuses().forEach(rs -> rs.setReceptionist(null));
            user.getReferenceStatuses().clear();
        }

        // 6. Filialdan uzish (optional)
        user.setFilials(null);

        // 7. Roles tozalash (ixtiyoriy)
        if (user.getRoles() != null) {
            user.getRoles().clear();
        }

        // 8. Endi userni o‘chiramiz
        userRepo.delete(user);
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
