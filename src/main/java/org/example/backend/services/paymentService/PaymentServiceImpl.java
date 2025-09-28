package org.example.backend.services.paymentService;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.PaymentDto;
import org.example.backend.dtoResponse.PaymentInfoResDto;
import org.example.backend.entity.Payment;
import org.example.backend.entity.PaymentCourseInfo;
import org.example.backend.entity.User;
import org.example.backend.repository.PaymentCourseInfoRepo;
import org.example.backend.repository.PaymentRepo;
import org.example.backend.repository.UserRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserRepo userRepo;
    private final PaymentRepo paymentRepo;
    private final PaymentCourseInfoRepo paymentCourseInfoRepo;

    @Override
    public void addPayment(PaymentDto paymentDto) {
        User user = userRepo.findById(paymentDto.getStudentId()).get();

        Payment payment = new Payment();
        payment.setStudent(user);
        payment.setDate(LocalDate.now());
        payment.setPaidAmount(paymentDto.getAmount());
        paymentRepo.save(payment);

    }

    @Override
    public void addPaymentInfo(String day, Integer amount) {
        int paymentDay = Integer.parseInt(day);

        // Kun 1 dan 31 oralig'ida bo'lishi kerak
        if (paymentDay < 1 || paymentDay > 31) {
            throw new IllegalArgumentException("Kiritilgan kun noto‘g‘ri: " + paymentDay);
        }

        PaymentCourseInfo paymentCourseInfo = paymentCourseInfoRepo.findFirstBy()
                .orElse(new PaymentCourseInfo());

        paymentCourseInfo.setPaymentDay(paymentDay);
        paymentCourseInfo.setCoursePaymentAmount(amount);
        paymentCourseInfoRepo.save(paymentCourseInfo);
    }

    @Override
    public PaymentInfoResDto getPaymentCourseInfo() {
        // Repo orqali barcha malumotlarni olish
        List<PaymentCourseInfo> all = paymentCourseInfoRepo.findAll();

        if (all.isEmpty()) {
            throw new RuntimeException("PaymentCourseInfo topilmadi!");
        }

        // Jadvalda faqat 1 ta bo‘lgani uchun 0-index ni olamiz
        PaymentCourseInfo entity = all.get(0);

        // DTO ga map qilamiz
        PaymentInfoResDto dto = new PaymentInfoResDto();
        dto.setAmount(entity.getCoursePaymentAmount());
        dto.setPaymentDay(entity.getPaymentDay());

        return dto;
    }
}
