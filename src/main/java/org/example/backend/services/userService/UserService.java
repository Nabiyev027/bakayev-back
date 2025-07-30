package org.example.backend.services.userService;

import org.example.backend.dto.LoginDto;
import org.example.backend.dto.StudentDto;
import org.example.backend.dto.UpdateUserDto;
import org.example.backend.dtoResponse.EmployerResDto;
import org.example.backend.dtoResponse.StudentResDto;
import org.example.backend.dtoResponse.TeacherNameDto;
import org.example.backend.dtoResponse.TeacherResDto;
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

    List<StudentResDto> getStudents();

    List<TeacherResDto> getTeachersWithData();

    List<Role> getEmpRoles();

    List<EmployerResDto> getEmployers();
}

