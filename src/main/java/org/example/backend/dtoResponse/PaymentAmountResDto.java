package org.example.backend.dtoResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentAmountResDto {
    private int numPayments;
    private int paymentAmount;
}
