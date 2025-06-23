package org.example.backend.services.userService;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoginDto;
import org.example.backend.dto.UpdateUserDto;
import org.example.backend.dto.UserRegisterDto;
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
                                   Integer discount, String discountTitle, MultipartFile image) {
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
    public Optional<User> registerForAdmin(String firstName, String lastName, String phone, String username, String password, String filialId, String role, MultipartFile image) {
        Optional<Role> roleOpt = roleRepo.findByName(role);
        if (roleOpt.isEmpty()) {
            return Optional.empty();
        }

        Role role1 = roleOpt.get();

        // Filialni topamiz
        Optional<Filial> filialOpt = filialRepo.findById(UUID.fromString(filialId));
        if (filialOpt.isEmpty()) {
            return Optional.empty(); // Agar filial topilmasa, foydalanuvchi yaratmaslik
        }

        Filial filial = filialOpt.get();

        User userNew = new User();
        userNew.setFirstName(firstName);
        userNew.setLastName(lastName);
        userNew.setUsername(username);
        userNew.setPhone(phone);
        userNew.setPassword(passwordEncoder.encode(password));
        userNew.setRoles(List.of(role1));

        if (image != null && !image.isEmpty()) {
            String imgPath = createImage(image);
            userNew.setImageUrl(imgPath);
        }

        // Filialni biriktiramiz
        userNew.setFilials(List.of(filial));

        // Userni saqlaymiz
        User savedUser = userRepo.save(userNew);

        return Optional.of(savedUser);
    }




    @Override
    public void updateUser(UUID id, UpdateUserDto updateUserDto) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("Foydalanuvchi topilmadi: " + id);
        }

        User user = optionalUser.get();

        // Yangilanishi kerak bo'lgan maydonlarni tekshirib yangilash
        if (updateUserDto.getFirstName() != null) {
            user.setFirstName(updateUserDto.getFirstName());
        }

        if (updateUserDto.getLastName() != null) {
            user.setLastName(updateUserDto.getLastName());
        }

        if (updateUserDto.getUsername() != null) {
            user.setUsername(updateUserDto.getUsername());
        }

        if (updateUserDto.getPhone() != null) {
            user.setPhone(updateUserDto.getPhone());
        }

        if (updateUserDto.getPassword() != null) {
            user.setParentPhone(updateUserDto.getParentPhone());
        }

        // Foydalanuvchini yangilash
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
    public void deleteUser(UUID id) {
        userRepo.deleteById(id);
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
