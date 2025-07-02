package org.example.backend.services.userService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoginDto;
import org.example.backend.dto.StudentDto;
import org.example.backend.dto.UpdateUserDto;
import org.example.backend.dtoResponse.FilialNameDto;
import org.example.backend.dtoResponse.GroupsNamesDto;
import org.example.backend.dtoResponse.StudentResDto;
import org.example.backend.dtoResponse.TeacherNameDto;
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
        userNew.setFilial(filial);

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
        user.setFilial(filialOpt.get());

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
    public List<StudentResDto> getStudents() {
        List<StudentResDto> students = new ArrayList<>();

        Optional<Role> roleOpt = roleRepo.findByName("ROLE_STUDENT");
        if (roleOpt.isEmpty()) {
            System.out.println("ROLE_STUDENT not found!");
            return students;
        }

        Role studentRole = roleOpt.get();
        List<User> roleStudents = userRepo.getByRoles(List.of(studentRole));

        System.out.println("Found students count: " + roleStudents.size());

        roleStudents.forEach(s -> {
            StudentResDto newStudent = new StudentResDto();
            newStudent.setId(s.getId());
            newStudent.setImgUrl(s.getImageUrl());
            newStudent.setFirstName(s.getFirstName());
            newStudent.setLastName(s.getLastName());
            newStudent.setUsername(s.getUsername());
            newStudent.setPhone(s.getPhone());
            newStudent.setParentPhone(s.getParentPhone());

            Filial filial = s.getFilial();
            if (filial != null) {
                FilialNameDto filialNameDto = new FilialNameDto();
                filialNameDto.setId(filial.getId());
                filialNameDto.setName(filial.getName());
                newStudent.setFilialNameDto(filialNameDto);
            }

            List<GroupsNamesDto> groups = new ArrayList<>();
            if (s.getStudentGroups() != null) {
                s.getStudentGroups().forEach(group -> {
                    GroupsNamesDto groupsNames = new GroupsNamesDto();
                    groupsNames.setId(group.getId());
                    groupsNames.setName(group.getName());
                    groups.add(groupsNames);
                });
            }

            newStudent.setGroups(groups);
            students.add(newStudent);
        });

        return students;
    }

    @Transactional
    @Override
    public void updateStudent(UUID id, StudentDto studentDto) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("Foydalanuvchi topilmadi: " + id);
        }

        User user = optionalUser.get();

        // Ism va familiya va boshqa oddiy maydonlarni yangilash
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
            user.setFilial(filial);
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
        user.setFilial(null);

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

            String uniqueFileName = UUID.randomUUID().toString() + "_" + img.getOriginalFilename();
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

    private boolean hasRole(List<Role> roles, String authority) {
       return roles.stream().filter(role -> role.getName().equals(authority)).toList().size()>0;
    }

}
