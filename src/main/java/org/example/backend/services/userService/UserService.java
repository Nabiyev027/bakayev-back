package org.example.backend.services.userService;

import org.example.backend.dto.*;
import org.example.backend.dtoResponse.*;
import org.example.backend.entity.Role;
import org.example.backend.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    User getUserByUsername(String username);
    Map<?, ?> login(LoginDto loginDto);

    void deleteUser(UUID id);

    void changeLoginPassword(UUID id,String oldPassword, String newPassword);

    Optional<User> register(String firstName,
                            String lastName, String phone, String parentPhone, String username,
                            String password, String groupId, String role, Integer discount,
                            Integer discountTime,Integer teacherSalary,Integer receptionSalary,
                            MultipartFile image, String filialId);

    void updateStudent(UUID id, StudentDto studentDto);


    List<Role> getRoles();


    List<TeacherNameDto> getTeachers();

    List<StudentResDto> getStudentsWithData(String filialId, String groupId);

    List<TeacherResDto> getTeachersWithData(String filialId);

    List<Role> getEmpRoles();

    List<EmployerResDto> getEmployers(String filialId, String roleId);

    void updateTeacher(UUID id, TeacherDto teacherDto);

    void updateEmployer(UUID id, EmployerDto employerDto);

    List<StudentForMessageResDto> getStudentForMessage(UUID filialId, UUID groupId);

    List<StudentNameResDto> getStudentsByGroup(UUID groupId);

    void changeEmployerPassword(UUID userId, String newPassword);

    List<TeacherNameDto> getTeachersByFilial(UUID filialId);

    List<AdminResDto> getAdminsWithData();

    void registerForSuperAdmin(String firstName, String lastName, String username, String password);

    void updateForSuperAdmin(UUID id, String firstName, String lastName, String username, String password);

    UserInfoResDto getUserInfo(UUID id);

    void updateUserInfo(UUID id, String firstName, String lastName, String username, MultipartFile img);

    List<StudentInfoResDto> getStudentInfos(UUID id);

    void updateStatus(UUID id, String status, UUID groupId);

    List<DiscountResDto> getStudentDiscounts(UUID id);

    void addDiscount(UUID id, DiscountDto discountDto);

    void editDiscount(UUID id, DiscountDto discountDto);

    void deleteDiscount(UUID id);
}

