package org.example.backend.services.userService;

import org.example.backend.dto.EmployerDto;
import org.example.backend.dto.LoginDto;
import org.example.backend.dto.StudentDto;
import org.example.backend.dto.TeacherDto;
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

    Optional<User> register(String firstName, String lastName, String phone, String parentPhone, String username, String password, String groupId, String role, Integer discount, String discountTitle, MultipartFile image, String filialId);

    void updateStudent(UUID id, StudentDto studentDto);


    List<Role> getRoles();

    Optional<User> registerForAdmin(String firstName, String lastName, String phone, String username, String password, String filialId, String role, String groupId, MultipartFile image);

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

}

