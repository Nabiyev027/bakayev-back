package org.example.backend.dtoResponse;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class PaymentResDto {
    private UUID id;
    private String fullName;
    private LocalDate paymentDate;
    private Integer paidAmount;
    private Integer discountAmount;
    private String paymentStatus;
    private List<PaymentTransactionResDto> transactions;

}
