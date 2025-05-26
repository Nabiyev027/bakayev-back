package org.example.backend.dto;

import lombok.Data;

@Data
public class UpdateUserDto {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String phone;
    private String parentPhone;
}
