package org.example.backend.dtoResponse;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class StudentForMessageResDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String phone;
    private String parentPhone;
    private String paid;
    private Integer debt;
}
