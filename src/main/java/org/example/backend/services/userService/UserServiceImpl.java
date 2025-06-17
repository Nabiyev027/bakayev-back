package org.example.backend.services.userService;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoginDto;
import org.example.backend.dto.UpdateUserDto;
import org.example.backend.dto.UserRegisterDto;
import org.example.backend.entity.Discount;
import org.example.backend.entity.Group;
import org.example.backend.entity.Role;
import org.example.backend.entity.User;
import org.example.backend.repository.DiscountRepo;
import org.example.backend.repository.GroupRepo;
import org.example.backend.repository.RoleRepo;
import org.example.backend.repository.UserRepo;
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

    @Override
    @Transactional
    public Optional<User> register(UserRegisterDto dto) {

        Optional<User> user = userRepo.findById(dto.getUserId());

        if (user.isEmpty()) {
            return Optional.empty(); // foydalanuvchi topilmagan
        }

        User foundUser = user.get();

        if (hasRole(foundUser.getRoles(), "ROLE_RECEPTION")) {
            Optional<Role> roleUser = roleRepo.findByName("ROLE_STUDENT");
            if (roleUser.isEmpty()) return Optional.empty(); // rol topilmasa

            User userNew = new User();
            userNew.setFirstName(dto.getFirstName());
            userNew.setLastName(dto.getLastName());
            userNew.setUsername(dto.getUsername());
            userNew.setPhone(dto.getPhone());
            userNew.setParentPhone(dto.getParentPhone());
            userNew.setPassword(passwordEncoder.encode(dto.getPassword()));
//            String imgPath = createImage(dto.getImg());
//            userNew.setImageUrl(imgPath);
            userNew.setRoles(List.of(roleUser.get()));

            if (dto.getGroupId() != null) {
                groupRepo.findById(dto.getGroupId()).ifPresent(group -> userNew.setStudentGroups(List.of(group)));
            }

            User save = userRepo.save(userNew);

            if (dto.getDiscount() != null && dto.getDiscount() != 0) {
                Discount discount = new Discount();
                discount.setQuantity(dto.getDiscount());
                discount.setTitle(dto.getDiscountTitle());
                discount.setStudent(save);
                // discountRepo.save(discount); ← agar kerak bo‘lsa saqlashni unutmang
            }

            return Optional.of(save);



        } else if (hasRole(foundUser.getRoles(), "ROLE_STUDENT")) {
            Optional<Role> roleUser = roleRepo.findByName("ROLE_RECEPTION");
            if (roleUser.isEmpty()) return Optional.empty();

            User userNew = new User();
            userNew.setFirstName(dto.getFirstName());
            userNew.setLastName(dto.getLastName());
            userNew.setUsername(dto.getUsername());
            userNew.setPhone(dto.getPhone());
            userNew.setParentPhone(dto.getParentPhone());
            userNew.setPassword(passwordEncoder.encode(dto.getPassword()));
            String imgPath = createImage(dto.getImg());
            userNew.setImageUrl(imgPath);
            userNew.setRoles(List.of(roleUser.get()));

            if (dto.getGroupId() != null) {
                groupRepo.findById(dto.getGroupId()).ifPresent(group -> userNew.setStudentGroups(List.of(group)));
            }

            User save = userRepo.save(userNew);
            return Optional.of(save);
        }

        return Optional.empty();
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
            // static/uploads papkasi joylashgan manzilni olish
            File uploadsFolder = new ClassPathResource("static/uploads/").getFile();

            // Agar papka mavjud bo'lmasa - yaratamiz
            if (!uploadsFolder.exists()) {
                uploadsFolder.mkdirs();
            }

            // Unikal fayl nomi yaratamiz
            String uniqueFileName = UUID.randomUUID().toString() + "_" + img.getOriginalFilename();

            // Faylni to'liq yo'liga saqlaymiz
            File destination = new File(uploadsFolder, uniqueFileName);
            img.transferTo(destination);

            // Frontendda ko‘rsatish uchun nisbiy yo‘lni qaytaramiz
            return "/uploads/" + uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Rasmni saqlab bo‘lmadi", e);
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
