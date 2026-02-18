package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.*;
import org.example.backend.dtoResponse.*;
import org.example.backend.entity.Role;
import org.example.backend.services.userService.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_MAIN_RECEPTION','ROLE_TEACHER','ROLE_STUDENT','ROLE_ADMIN')")
    @GetMapping("/discount/student/{id}")
    public ResponseEntity<?> getStudentDiscounts(@PathVariable UUID id) {
        try {
            List<DiscountResDto> discountList = userService.getStudentDiscounts(id);
            return ResponseEntity.ok(discountList);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @PostMapping("/discount/add/{id}")
    public ResponseEntity<?> addNewDiscount(@PathVariable UUID id, @RequestBody DiscountDto discountDto) {
        try {
            userService.addDiscount(id,discountDto);
            return ResponseEntity.ok("Discount added successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @PutMapping("/discount/update/{id}")
    public ResponseEntity<?> editStudentDiscount(@PathVariable UUID id, @RequestBody DiscountDto discountDto) {
        try {
            userService.editDiscount(id,discountDto);
            return ResponseEntity.ok("Discount added successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @DeleteMapping("/discount/{id}")
    public ResponseEntity<?> deleteDiscount(@PathVariable UUID id) {
        try {
            userService.deleteDiscount(id);
            return ResponseEntity.ok("Discount deleted successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_TEACHER','ROLE_MAIN_RECEPTION'," +
            "'ROLE_TEACHER','ROLE_STUDENT','ROLE_ADMIN')")
    @GetMapping("/getInfo/{id}")
    public ResponseEntity<?> getUserInfo(@PathVariable UUID id) {
        try {
            UserInfoResDto user = userService.getUserInfo(id);
            return ResponseEntity.ok(user);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_TEACHER','ROLE_TEACHER','ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @GetMapping("/studentInfo/{id}")
    public ResponseEntity<?> getStudent(@PathVariable UUID id) {
        try {
            List<StudentInfoResDto> infos = userService.getStudentInfos(id);
            return ResponseEntity.ok(infos);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_MAIN_RECEPTION','ROLE_TEACHER','ROLE_ADMIN')")
    @PutMapping("/updateStatus/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable UUID id, @RequestParam String status, @RequestParam UUID groupId) {
        try {
            userService.updateStatus(id,status,groupId);
            return ResponseEntity.ok("User status updated");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_MAIN_RECEPTION','ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @PutMapping("/updateInfo/{id}")
    public ResponseEntity<?> updateUserInfo(@PathVariable UUID id,
                                            @RequestParam("firstName") String firstName,
                                            @RequestParam("lastName") String lastName,
                                            @RequestParam("username") String username,
                                            @RequestParam(value = "img", required = false) MultipartFile img
                                            ) {
        try {
            userService.updateUserInfo(id,firstName,lastName,username, img);
            return ResponseEntity.ok("Settings updated successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_TEACHER','ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @GetMapping("/getRoles")
    public ResponseEntity<?> getRoles() {
        try {
            List<Role> roles = userService.getRoles();
            return ResponseEntity.ok(roles);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @GetMapping("/getEmpRoles")
    public ResponseEntity<?> getEmpRoles(){
        try {
            List<Role> empRoles = userService.getEmpRoles();
            return ResponseEntity.ok(empRoles);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
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

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @GetMapping("/teacher")
    public ResponseEntity<?> getTeachers() {
        try {
            List<TeacherNameDto> teachers = userService.getTeachers();
            return ResponseEntity.ok(teachers);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @GetMapping("/teacher/{filialId}")
    public ResponseEntity<?> getTeachers(@PathVariable UUID filialId) {
        try {
            List<TeacherNameDto> teachers = userService.getTeachersByFilial(filialId);
            return ResponseEntity.ok(teachers);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_TEACHER','ROLE_STUDENT','ROLE_MAIN_RECEPTION','ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable UUID userId) {
        try {
            UserReception userInfoByLogin = userService.getUserInfoByLogin(userId);
            return ResponseEntity.ok(userInfoByLogin);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    @GetMapping("/admins")
    public ResponseEntity<?> getTeachersWithData() {
        try {
            List<AdminResDto> admins = userService.getAdminsWithData();
            return  ResponseEntity.ok(admins);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
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

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_TEACHER','ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @GetMapping("/students")
    public ResponseEntity<?> getStudentsWithData(
            @RequestParam(required = false) String filialId,
            @RequestParam(required = false) String groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size

    ) {
        try {
            Page<StudentResDto> students = userService.getStudentsWithData(filialId, groupId, page, size);
            return  ResponseEntity.ok(students);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_TEACHER','ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @GetMapping("/student")
    public ResponseEntity<?> getStudentByGroup(@RequestParam UUID groupId){
        try {
            List<StudentNameResDto> students = userService.getStudentsByGroup(groupId);
            return  ResponseEntity.ok(students);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @GetMapping("/studentsForMessage")
    public ResponseEntity<?> getStudentsForMessage(@RequestParam UUID filialId, @RequestParam UUID groupId){
        try {
            List<StudentForMessageResDto> list = userService.getStudentForMessage(filialId, groupId);
            return ResponseEntity.ok(list);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_MAIN_RECEPTION','ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    @PutMapping("/settings/{id}")
    public ResponseEntity<?> changePassword(@PathVariable UUID id, @RequestParam String oldPassword, @RequestParam String newPassword){
        try {
            userService.changeLoginPassword(id, oldPassword, newPassword);
            return ResponseEntity.ok("Successfully changed");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @PutMapping("/changePassword/{userId}")
    public ResponseEntity<?> changePasswordForAdmin(@PathVariable UUID userId, @RequestParam String newPassword){
        try {
            userService.changeEmployerPassword(userId, newPassword);
            return ResponseEntity.ok("Password successfully changed");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_TEACHER','ROLE_MAIN_RECEPTION','ROLE_ADMIN','ROLE_SUPER_ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id){
        try {
            userService.deleteUser(id);
            return  ResponseEntity.ok("User deleted successfully");
        }catch (Exception e){
            e.printStackTrace();
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
