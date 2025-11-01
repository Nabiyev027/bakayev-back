package org.example.backend.dtoResponse;

import lombok.Data;

import java.time.LocalDate;
@Data
public class PaymentTransactionResDto {
    private LocalDate paymentDate;
    private Integer paidAmount;
    private String paymentMethod;
}
