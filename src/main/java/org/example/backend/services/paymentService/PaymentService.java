package org.example.backend.services.paymentService;

import org.example.backend.dto.PaymentDto;
import org.example.backend.dtoResponse.PaymentAmountResDto;
import org.example.backend.dtoResponse.PaymentInfoResDto;
import org.example.backend.dtoResponse.PaymentResDto;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    void addPayment(PaymentDto paymentDto);

    void addPaymentInfo(String day, Integer amount);

    PaymentInfoResDto getPaymentCourseInfo();

    List<PaymentResDto> getPaymentsWithTransaction(UUID groupId, String dateFrom, String dateTo, String paymentMethod);

    List<PaymentResDto> getUserPaymentsWithTransactions(UUID studentId, String dateFrom, String dateTo, String paymentMethod);

    List<PaymentResDto> getPayments(UUID id);

    Integer getPaymentInfo(UUID id);

    List<PaymentAmountResDto> getPaymentAmounts(String id);
}
