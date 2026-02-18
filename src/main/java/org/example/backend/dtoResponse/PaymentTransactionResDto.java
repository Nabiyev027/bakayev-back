package org.example.backend.dtoResponse;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class PaymentTransactionResDto {
    private UUID id;
    private LocalDate paymentDate;
    private Integer paidAmount;
    private String paymentMethod;
}
