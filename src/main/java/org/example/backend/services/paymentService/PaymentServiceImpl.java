package org.example.backend.services.paymentService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.PaymentMethod;
import org.example.backend.Enum.PaymentStatus;
import org.example.backend.dto.PaymentDto;
import org.example.backend.dtoResponse.PaymentInfoResDto;
import org.example.backend.entity.*;
import org.example.backend.repository.*;
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
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final DebtsRepo debtsRepo;

    @Override
    public void addPayment(PaymentDto paymentDto) {
        User user = userRepo.findById(paymentDto.getStudentId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PaymentCourseInfo paymentCourseInfo = paymentCourseInfoRepo.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment course Info not found"));

        Integer courseAmount = paymentCourseInfo.getCoursePaymentAmount();
        Integer paidAmount = paymentDto.getAmount();

        // To'lov metodi
        String method = PaymentMethod.CARD.toString().equals(paymentDto.getPaymentMethod().toUpperCase())
                ? PaymentMethod.CARD.toString()
                : PaymentMethod.CASH.toString();

        // Nechta oy uchun to'langanini hisoblaymiz
        int fullMonths = paidAmount / courseAmount;
        int remainder = paidAmount % courseAmount;

        // Har bir oy uchun payment yozamiz
        for (int i = 0; i < fullMonths; i++) {
            Payment monthlyPayment = new Payment();
            monthlyPayment.setStudent(user);
            monthlyPayment.setDate(LocalDate.now().plusMonths(i)); // keyingi oylar
            monthlyPayment.setPaymentStatus(PaymentStatus.PAID);
            monthlyPayment.setPaidAmount(courseAmount);

            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setAmount(courseAmount);
            transaction.setTransactionDate(LocalDate.now());
            transaction.setPaymentMethod(PaymentMethod.valueOf(method));
            transaction.setPayment(monthlyPayment);

            paymentRepo.save(monthlyPayment);
            paymentTransactionRepo.save(transaction);
        }

        // Agar ortib qolgan qismi bo'lsa (to'liq oy summasiga yetmagan)
        if (remainder > 0) {
            Payment nextPayment = new Payment();
            nextPayment.setStudent(user);
            nextPayment.setDate(LocalDate.now().plusMonths(fullMonths)); // keyingi oy
            nextPayment.setPaymentStatus(PaymentStatus.PENDING); // to'liq emas
            nextPayment.setPaidAmount(remainder);

            PaymentTransaction nextTransaction = new PaymentTransaction();
            nextTransaction.setAmount(remainder);
            nextTransaction.setTransactionDate(LocalDate.now());
            nextTransaction.setPaymentMethod(PaymentMethod.valueOf(method));
            nextTransaction.setPayment(nextPayment);

            paymentRepo.save(nextPayment);
            paymentTransactionRepo.save(nextTransaction);

            // qarzdorlik yozamiz
            int debt = courseAmount - remainder;
            Debts debts = new Debts();
            debts.setAmount(debt);
            debts.setStudent(user);
            debtsRepo.save(debts);
        }
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
