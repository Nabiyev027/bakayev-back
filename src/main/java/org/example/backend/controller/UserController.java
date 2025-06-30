package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.UpdateUserDto;
import org.example.backend.dto.UserReception;
import org.example.backend.dtoResponse.TeacherNameDto;
import org.example.backend.entity.Role;
import org.example.backend.entity.User;
import org.example.backend.repository.UserRepo;
import org.example.backend.services.userService.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {
    private final UserService userService;
    private final UserRepo userRepo;

    @GetMapping("/getRoles")
    public ResponseEntity<?> getRoles() {
        try {
            List<Role> roles = userService.getRoles();
            return ResponseEntity.ok(roles);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/teacher")
    public ResponseEntity<?> getTeachers() {
        try {
            List<TeacherNameDto> teachers = userService.getTeachers();
            return ResponseEntity.ok(teachers);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        try {
            User user = userRepo.findById(id).orElseThrow();
            UserReception userReception = new UserReception();
            userReception.setFullName(user.getFirstName()+" "+user.getLastName());
            userReception.setUsername(user.getUsername());
            userReception.setPhone(user.getPhone());
            userReception.setRoles(user.getRoles());

            return ResponseEntity.ok(userReception);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/student")
    public ResponseEntity<?> getStudents(){
        try {
            return  ResponseEntity.ok(userRepo.findAll());
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/settings/{id}")
    public ResponseEntity<?> changePassword(@PathVariable UUID id, @RequestParam String oldPassword, @RequestParam String newPassword){
        try {
            userService.changeLoginPassword(id, oldPassword, newPassword);
            return ResponseEntity.ok("Successfully changed");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id){
        try {
            userService.deleteUser(id);
            return  ResponseEntity.ok("User deleted successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @RequestBody UpdateUserDto updateUserDto){
        try {
            userService.updateUser(id,updateUserDto);
            return ResponseEntity.ok("User updated successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
