package org.example.backend.dto;

import lombok.Data;

@Data
public class PaymentDto {
    private String cardNumber;
    private String expiryMonth;
    private String expiryYear;
    private String studentId;
}
