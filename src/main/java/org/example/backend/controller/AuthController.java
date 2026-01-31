package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoginDto;
import org.example.backend.security.service.JwtService;
import org.example.backend.services.userService.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam("firstName") String firstName,
                                  @RequestParam("lastName") String lastName,
                                  @RequestParam("phone") String phone,
                                  @RequestParam(value = "parentPhone", required = false) String parentPhone,
                                  @RequestParam("username") String username,
                                  @RequestParam("password") String password,
                                  @RequestParam(value = "groupId", required = false) String groupId,
                                  @RequestParam("role") String role,
                                  @RequestParam(value = "discount", required = false) Integer discount,
                                  @RequestParam(value = "discountTime", required = false) Integer discountTime,
                                  @RequestParam(value = "teacherSalary", required = false) Integer teacherSalary,
                                  @RequestParam(value = "receptionSalary", required = false) Integer receptionSalary,
                                  @RequestParam(value = "image", required = false) MultipartFile image,
                                  @RequestParam("filialId") String filialId ) {
        try {
            userService.register(firstName,lastName,phone,parentPhone,username,password,groupId,role,discount,discountTime,teacherSalary,receptionSalary,image, filialId);
            return ResponseEntity.ok("User registered successfully");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/registerForSuper")
    public ResponseEntity<?> registerSuperAdmin(
            @RequestParam("firstName") String firstName,
                                       @RequestParam("lastName") String lastName,
                                       @RequestParam("username") String username,
                                       @RequestParam("password") String password){
        try {
            userService.registerForSuperAdmin(firstName,lastName,username,password);
            return ResponseEntity.ok("Admin registered successfully");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/updateForSuper/{id}")
    public ResponseEntity<?> updateSuperAdmin(@PathVariable UUID id,
                                              @RequestParam("firstName") String firstName,
                                              @RequestParam("lastName") String lastName,
                                              @RequestParam("username") String username,
                                              @RequestParam(value = "password", required = false) String password) {
        try {
            userService.updateForSuperAdmin(id, firstName, lastName, username, password);
            return ResponseEntity.ok("Admin updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }




    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        try {
            Map<?, ?> login = userService.login(loginDto);
            return ResponseEntity.ok(login);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("key") String refreshToken){
        System.out.println(refreshToken);
        return  jwtService.refreshToken(refreshToken);
    }

}
