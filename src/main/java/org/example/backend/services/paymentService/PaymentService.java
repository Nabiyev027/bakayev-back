package org.example.backend.services.paymentService;

import org.example.backend.dto.PaymentDto;
import org.example.backend.dtoResponse.PaymentInfoResDto;
import org.example.backend.entity.PaymentCourseInfo;

import java.time.LocalDate;

public interface PaymentService {
    void addPayment(PaymentDto paymentDto);

    void addPaymentInfo(String day, Integer amount);

    PaymentInfoResDto getPaymentCourseInfo();
}
