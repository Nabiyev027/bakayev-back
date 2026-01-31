package org.example.backend.dtoResponse;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class SalaryReceptionRes {
    private UUID id;
    private String fullName;
    private double salaryAmount;
    private double paidAmount;
    private LocalDate date;
}
