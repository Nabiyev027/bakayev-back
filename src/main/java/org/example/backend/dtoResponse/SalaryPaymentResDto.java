package org.example.backend.dtoResponse;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class SalaryPaymentResDto {
    private UUID id;
    private String groupName;
    private Integer amount;
    private LocalDate date;
}
