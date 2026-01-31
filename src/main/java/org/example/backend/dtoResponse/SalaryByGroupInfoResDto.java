package org.example.backend.dtoResponse;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class SalaryByGroupInfoResDto {
    private UUID id;
    private String groupName;
    private Integer percentage;
    private Integer amount;
    private Integer mustPaid;
    private LocalDate date;
}
