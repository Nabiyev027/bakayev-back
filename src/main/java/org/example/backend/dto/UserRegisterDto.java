package org.example.backend.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class UserRegisterDto {
    private String firstName;
    private String lastName;
    private String phone;
    private String parentPhone;
    private String username;
    private String password;
    private UUID userId;
    private UUID groupId;
    private String role;
    private String discountTitle;
    private Integer discount;
    private MultipartFile img;
}
