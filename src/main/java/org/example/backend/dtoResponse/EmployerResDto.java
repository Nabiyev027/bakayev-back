package org.example.backend.dtoResponse;

import lombok.Data;
import org.example.backend.entity.Role;

import java.util.List;
import java.util.UUID;

@Data
public class EmployerResDto {
    private UUID id;
    private String imgUrl;
    private String firstName;
    private String lastName;
    private String phone;
    private FilialNameDto filialNameDto;
    private List<Role> roles;
    private String username;
}
