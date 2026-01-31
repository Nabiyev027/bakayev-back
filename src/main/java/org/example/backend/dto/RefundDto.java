package org.example.backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class RefundDto {
    private UUID studentId;
    private UUID receptionId;
    private Integer amount;
}
