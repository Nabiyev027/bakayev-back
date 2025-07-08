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
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    @Override
    public void run(String... args) throws Exception {
        List<Role> all = roleRepo.findAll();
        if(all.isEmpty()){
            List<Role> roles=List.of(
                    Role.builder().name("ROLE_ADMIN").build(),
                    Role.builder().name("ROLE_RECEPTION").build(),
                    Role.builder().name("ROLE_TEACHER").build(),
                    Role.builder().name("ROLE_MAIN_RECEPTION").build(),
                    Role.builder().name("ROLE_STUDENT").build()
            );
            roleRepo.saveAll(roles);
            Role roleAdmin = roleRepo.findByName("ROLE_ADMIN").orElseThrow();
            User userA=new User();
            userA.setFirstName("Bakayev");
            userA.setLastName("Sohib");
            userA.setPhone("+998914420031");
            userA.setUsername("bakayev");
            userA.setPassword(passwordEncoder.encode("123"));
            userA.setRoles(List.of(roleAdmin));
            userRepo.save(userA);

            Role roleReception = roleRepo.findByName("ROLE_RECEPTION").orElseThrow();
            User userR =  new User();
            userR.setFirstName("Rajabov");
            userR.setLastName("Lazizbek");
            userR.setPhone("+998914420031");
            userR.setUsername("rajabov");
            userR.setPassword(passwordEncoder.encode("123"));
            userR.setRoles(List.of(roleReception));
            userRepo.save(userR);

            Role roleTeacher = roleRepo.findByName("ROLE_TEACHER").orElseThrow();
            User userT =  new User();
            userT.setFirstName("Umarov");
            userT.setLastName("Bekzod");
            userT.setPhone("+998914420031");
            userT.setUsername("umarov");
            userT.setPassword(passwordEncoder.encode("123"));
            userT.setRoles(List.of(roleTeacher));
            userRepo.save(userT);


        }

    }


}
