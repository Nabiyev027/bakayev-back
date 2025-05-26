package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoginDto;
import org.example.backend.dto.UserRegisterDto;
import org.example.backend.entity.User;
import org.example.backend.security.service.JwtService;
import org.example.backend.services.userService.UserService;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public HttpEntity<?> register(@ModelAttribute UserRegisterDto dto) {
        try {
            Optional<User> register = userService.register(dto);
            register.orElseThrow();
            return ResponseEntity.status(200).body("Registered user - " + register.get());
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
        return   jwtService.refreshToken(refreshToken);
    }


}
