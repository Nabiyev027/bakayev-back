package org.example.backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PaymentDto {
    private UUID studentId;
    private Integer amount;
    private String paymentMethod;
}
