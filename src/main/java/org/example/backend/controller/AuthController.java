package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoginDto;
import org.example.backend.dto.UserRegisterDto;
import org.example.backend.entity.User;
import org.example.backend.security.service.JwtService;
import org.example.backend.services.userService.UserService;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    @PostMapping("/register")
    public HttpEntity<?> register(@ModelAttribute UserRegisterDto dto) {

        Optional<User> optionalUser = userService.register(dto);

        System.out.println(optionalUser);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            System.out.println(dto);
            return ResponseEntity.status(200).body("Registered user - " + user);
        } else {
            return ResponseEntity.status(400).body("Foydalanuvchi ro'yxatdan o'tkazilmadi!");
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
        return   jwtService.refreshToken(refreshToken);
    }


}
