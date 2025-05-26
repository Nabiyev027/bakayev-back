package org.example.backend.dto;

import lombok.Data;
import org.example.backend.entity.Role;

import java.util.List;

@Data
public class UserReception {
    private String fullName;
    private String username;
    private String phone;
    private List<Role> roles;

}
