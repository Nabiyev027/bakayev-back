package org.example.backend.dtoResponse;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PaymentResDto {
    private String fullName;
    private LocalDate paymentDate;
    private Integer paidAmount;
    private Integer discountAmount;
    private String paymentStatus;
    private List<PaymentTransactionResDto> transactions;

}
