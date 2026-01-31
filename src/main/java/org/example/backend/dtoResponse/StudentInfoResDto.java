package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class StudentInfoResDto {
    private UUID id;
    private String name;
    private String status;
    private String paymentStatus;
    private Integer discount;
    private String endDate;
    private Integer debt;
}
