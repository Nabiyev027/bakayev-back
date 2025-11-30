package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AdminResDto {
    private UUID id;
    private String imgUrl;
    private String firstName;
    private String lastName;
    private String phone;
    private String username;
}
