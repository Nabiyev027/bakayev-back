package org.example.backend.security.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Role;
import org.example.backend.entity.User;
import org.example.backend.repository.RoleRepo;
import org.example.backend.repository.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class Loader implements CommandLineRunner {

    private final RoleRepo roleRepo;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ================= ROLELARNI QO‘SHISH =================
        createRoleIfNotExists("ROLE_ADMIN");
        createRoleIfNotExists("ROLE_SUPER_ADMIN");
        createRoleIfNotExists("ROLE_RECEPTION");
        createRoleIfNotExists("ROLE_TEACHER");
        createRoleIfNotExists("ROLE_MAIN_RECEPTION");
        createRoleIfNotExists("ROLE_STUDENT");


        // ================= USERLARNI QO‘SHISH =================
        createUserIfNotExists(
                "bakayev",
                "Bakayev",
                "Sohib",
                "+998 91 442 00 31",
                "123",
                "ROLE_ADMIN"
        );

        createUserIfNotExists(
                "admin",
                "admin",
                "admin",
                "+998 91 442 00 31",
                "123",
                "ROLE_SUPER_ADMIN"
        );

        createUserIfNotExists(
                "rajabov",
                "Rajabov",
                "Lazizbek",
                "+998 91 442 00 31",
                "123",
                "ROLE_RECEPTION"
        );

        createUserIfNotExists(
                "umarov",
                "Umarov",
                "Bekzod",
                "+998 91 442 00 31",
                "123",
                "ROLE_TEACHER"
        );

        createUserIfNotExists(
                "aliyev",
                "Aliyev",
                "Valijon",
                "+998 91 442 00 31",
                "123",
                "ROLE_STUDENT"
        );

        createUserIfNotExists(
                "karimov",
                "Karimov",
                "Axmed",
                "+998 91 442 00 31",
                "123",
                "ROLE_STUDENT"
        );
    }


    // ================= ROLE CREATE METHOD =================
    private void createRoleIfNotExists(String roleName) {
        if (roleRepo.findByName(roleName).isEmpty()) {
            Role role = Role.builder()
                    .name(roleName)
                    .build();
            roleRepo.save(role);
        }
    }


    // ================= USER CREATE METHOD =================
    private void createUserIfNotExists(
            String username,
            String firstName,
            String lastName,
            String phone,
            String rawPassword,
            String roleName
    ) {

        if (userRepo.findByUsername(username).isEmpty()) {

            Role role = roleRepo.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role topilmadi: " + roleName));

            User user = new User();
            user.setUsername(username);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhone(phone);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRoles(List.of(role));

            userRepo.save(user);
        }
    }
}
