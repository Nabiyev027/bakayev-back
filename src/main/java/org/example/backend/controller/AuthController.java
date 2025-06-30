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

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION')")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam("firstName") String firstName,
                                  @RequestParam("lastName") String lastName,
                                  @RequestParam("phone") String phone,
                                  @RequestParam(value = "parentPhone", required = false) String parentPhone,
                                  @RequestParam("username") String username,
                                  @RequestParam("password") String password,
                                  @RequestParam(value = "groupId", required = false) String groupId,
                                  @RequestParam("role") String role,
                                  @RequestParam(value = "discount", required = false) Integer discount,
                                  @RequestParam(value = "discountTitle", required = false) String discountTitle,
                                  @RequestParam(value = "image", required = false) MultipartFile image,
                                  @RequestParam("filialId") String filialId ) {
        try {
            userService.register(firstName,lastName,phone,parentPhone,username,password,groupId,role,discount,discountTitle, image, filialId);
            return ResponseEntity.ok("User registered successfully");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MAIN_RECEPTION')")
    @PostMapping("/registerA")
    public ResponseEntity<?> registerA(@RequestParam("firstName") String firstName,
                                       @RequestParam("lastName") String lastName,
                                       @RequestParam("phone") String phone,
                                       @RequestParam("username") String username,
                                       @RequestParam("password") String password,
                                       @RequestParam("filialId") String filialId,
                                       @RequestParam("role") String role,
                                       @RequestParam(value = "image", required = false) MultipartFile image){
        try {
            userService.registerForAdmin(firstName,lastName,phone,username,password,filialId,role,image);
            return ResponseEntity.ok("User registered successfully");
        }catch (Exception e) {
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
