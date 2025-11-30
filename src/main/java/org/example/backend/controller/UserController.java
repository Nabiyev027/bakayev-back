package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.*;
import org.example.backend.dtoResponse.*;
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

    @GetMapping("/getEmpRoles")
    public ResponseEntity<?> getEmpRoles(){
        try {
            List<Role> empRoles = userService.getEmpRoles();
            return ResponseEntity.ok(empRoles);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/employer")
    public ResponseEntity<?> getEmployer(
            @RequestParam(required = false, defaultValue = "all") String filialId,
            @RequestParam(required = false, defaultValue = "all") String roleId
            ){
        try {
            List<EmployerResDto> employers = userService.getEmployers(filialId, roleId);
            return ResponseEntity.ok(employers);
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

    @GetMapping("/teacher/{filialId}")
    public ResponseEntity<?> getTeachers(@PathVariable UUID filialId) {
        try {
            List<TeacherNameDto> teachers = userService.getTeachersByFilial(filialId);
            return ResponseEntity.ok(teachers);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        try {
            User user = userRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

            UserReception userReception = new UserReception();
            userReception.setFullName(user.getFirstName() + " " + user.getLastName());
            userReception.setUsername(user.getUsername());
            userReception.setPhone(user.getPhone());
            userReception.setRoles(user.getRoles());

            return ResponseEntity.ok(userReception);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/admins")
    public ResponseEntity<?> getTeachersWithData() {
        try {
            List<AdminResDto> admins = userService.getAdminsWithData();
            return  ResponseEntity.ok(admins);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/teachers")
    public ResponseEntity<?> getTeachersWithData(
            @RequestParam(required = false, defaultValue = "all") String filialId
    ) {
        try {
            List<TeacherResDto> teachers = userService.getTeachersWithData(filialId);
            return  ResponseEntity.ok(teachers);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/students")
    public ResponseEntity<?> getStudentsWithData(
            @RequestParam(required = false, defaultValue = "all") String filialId,
            @RequestParam(required = false, defaultValue = "all") String groupId
    ) {
        try {
            List<StudentResDto> students = userService.getStudentsWithData(filialId, groupId);
            return  ResponseEntity.ok(students);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/student")
    public ResponseEntity<?> getStudentByGroup(@RequestParam UUID groupId){
        try {
            List<StudentNameResDto> students = userService.getStudentsByGroup(groupId);
            return  ResponseEntity.ok(students);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/studentsForMessage")
    public ResponseEntity<?> getStudentsForMessage(@RequestParam UUID filialId, @RequestParam UUID groupId){
        try {
            List<StudentForMessageResDto> list = userService.getStudentForMessage(filialId, groupId);
            return ResponseEntity.ok(list);
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

    @PutMapping("/changePassword/{userId}")
    public ResponseEntity<?> changePasswordForAdmin(@PathVariable UUID userId, @RequestParam String newPassword){
        try {
            userService.changeEmployerPassword(userId, newPassword);
            return ResponseEntity.ok("Password successfully changed");
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
            e.printStackTrace(); // yoki log.error(...);
            throw new RuntimeException("User deletion failed: " + e.getMessage());
        }
    }

    @PutMapping("/student/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable UUID id, @RequestBody StudentDto studentDto){
        try {
            userService.updateStudent(id,studentDto);
            return ResponseEntity.ok("User updated successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/teacher/{id}")
    public ResponseEntity<?> updateTeacher(@PathVariable UUID id, @RequestBody TeacherDto teacherDto){
        try {
            userService.updateTeacher(id,teacherDto);
            return ResponseEntity.ok("Employer updated successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/employer/{id}")
    public ResponseEntity<?> updateEmployer(@PathVariable UUID id, @RequestBody EmployerDto employerDto){
        try {
            userService.updateEmployer(id,employerDto);
            return ResponseEntity.ok("User updated successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }




}
