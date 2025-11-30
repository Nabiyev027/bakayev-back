package org.example.backend.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class EmployerDto {
    private String firstName;
    private String lastName;
    private String phone;
    private String username;
    private List<UUID> filialIds;
    private List<UUID> roleIds;
}
