package org.example.backend.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class StudentDto {
    private String firstName;
    private String lastName;
    private String phone;
    private String parentPhone;
    private String username;
    private UUID filialId;
    private List<UUID> groupIds;
}
