package org.example.backend.services.userService;

import org.example.backend.dto.LoginDto;
import org.example.backend.dto.UpdateUserDto;
import org.example.backend.dto.UserRegisterDto;
import org.example.backend.entity.Role;
import org.example.backend.entity.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    User getUserByUsername(String username);
    Map<?, ?> login(LoginDto loginDto);

    void deleteUser(UUID id);

    void changeLoginPassword(UUID id,String oldPassword, String newPassword);

    Optional<User> register(UserRegisterDto dto);

    void updateUser(UUID id, UpdateUserDto updateUserDto);


    List<Role> getRoles();
}

