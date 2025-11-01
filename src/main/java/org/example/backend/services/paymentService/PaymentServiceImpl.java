package org.example.backend.services.paymentService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.PaymentMethod;
import org.example.backend.Enum.PaymentStatus;
import org.example.backend.dto.PaymentDto;
import org.example.backend.dtoResponse.PaymentInfoResDto;
import org.example.backend.dtoResponse.PaymentResDto;
import org.example.backend.dtoResponse.PaymentTransactionResDto;
import org.example.backend.entity.*;
import org.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserRepo userRepo;
    private final PaymentRepo paymentRepo;
    private final PaymentCourseInfoRepo paymentCourseInfoRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final DebtsRepo debtsRepo;
    private final GroupRepo groupRepo;

    @Transactional
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

        String method = PaymentMethod.CARD.toString().equals(paymentDto.getPaymentMethod().toUpperCase())
                ? PaymentMethod.CARD.toString()
                : PaymentMethod.CASH.toString();

        List<Debts> debts = user.getDebts();

        if(!debts.isEmpty()) {
            for (Debts debt : debts) {
                if (paidAmount > debt.getAmount()) {
                    Integer distinction = paidAmount - debt.getAmount();

                    // Eski qarzni yopamiz
                    debtsRepo.delete(debt);

                    Payment payment = paymentRepo.getPaymentByStudentAndPaymentStatus(user, PaymentStatus.PENDING);
                    if (payment == null) {
                        payment = new Payment();
                        payment.setStudent(user);
                        payment.setDate(LocalDate.now());
                    }
                    payment.setPaidAmount(courseAmount);
                    payment.setPaymentStatus(PaymentStatus.PAID);
                    Payment saved1 = paymentRepo.save(payment);

                    PaymentTransaction paymentTransaction = new PaymentTransaction();
                    paymentTransaction.setPaymentMethod(PaymentMethod.valueOf(method));
                    Integer nextPay = paidAmount - debt.getAmount();
                    paymentTransaction.setAmount(nextPay);
                    paymentTransaction.setTransactionDate(LocalDate.now());
                    paymentTransaction.setPayment(saved1);
                    paymentTransactionRepo.save(paymentTransaction);

                    // Agar ortiqcha to‘lov bo‘lsa
                    if (distinction > 0) {
                        int nextMonthDebt = courseAmount - distinction;

                        Payment paymentNextMonth = new Payment();
                        paymentNextMonth.setStudent(user);
                        paymentNextMonth.setPaidAmount(distinction);
                        paymentNextMonth.setPaymentStatus(PaymentStatus.PENDING);
                        paymentNextMonth.setDate(LocalDate.now().plusMonths(1));
                        Payment savedNext = paymentRepo.save(paymentNextMonth);

                        PaymentTransaction paymentTransactionForNextPayment = new PaymentTransaction();
                        paymentTransactionForNextPayment.setPaymentMethod(PaymentMethod.valueOf(method));
                        paymentTransactionForNextPayment.setAmount(distinction);
                        paymentTransactionForNextPayment.setTransactionDate(LocalDate.now());
                        paymentTransactionForNextPayment.setPayment(savedNext);
                        paymentTransactionRepo.save(paymentTransactionForNextPayment);

                        Debts newDebt = new Debts();
                        newDebt.setAmount(nextMonthDebt);
                        newDebt.setStudent(user);
                        debtsRepo.save(newDebt);
                    }
                } else if(paidAmount.equals(debt.getAmount())) {
                        debtsRepo.delete(debt);

                        Payment payment = paymentRepo.getPaymentByStudentAndPaymentStatus(user,PaymentStatus.PENDING);

                        if(payment != null){
                            payment.setPaidAmount(payment.getPaidAmount() + paidAmount);
                            payment.setPaymentStatus(PaymentStatus.PAID);
                            Payment saved = paymentRepo.save(payment);

                            PaymentTransaction monthPaymentTransaction = new PaymentTransaction();
                            monthPaymentTransaction.setPaymentMethod(PaymentMethod.valueOf(method));
                            monthPaymentTransaction.setAmount(paidAmount);
                            monthPaymentTransaction.setPayment(saved);
                            monthPaymentTransaction.setTransactionDate(LocalDate.now());
                            paymentTransactionRepo.save(monthPaymentTransaction);

                        }
                }else {

                    Integer distinction = debt.getAmount() - paidAmount;

                    if(!distinction.equals(0)){
                        debt.setAmount(distinction);
                        debtsRepo.save(debt);

                        Payment payment = paymentRepo.getPaymentByStudentAndPaymentStatus(user,PaymentStatus.PENDING);

                        if(payment != null){
                            payment.setPaidAmount(payment.getPaidAmount() + paidAmount);
                            payment.setPaymentStatus(PaymentStatus.PENDING);
                            Payment saved = paymentRepo.save(payment);

                            PaymentTransaction nextMonthPaymentTransaction = new PaymentTransaction();
                            nextMonthPaymentTransaction.setPaymentMethod(PaymentMethod.valueOf(method));
                            nextMonthPaymentTransaction.setAmount(paidAmount);
                            nextMonthPaymentTransaction.setPayment(saved);
                            nextMonthPaymentTransaction.setTransactionDate(LocalDate.now());
                            paymentTransactionRepo.save(nextMonthPaymentTransaction);



                        }

                    }


                }
            }
        } else {

            if(paidAmount > courseAmount){

                int distinction = paidAmount - courseAmount;

                Payment payment = new Payment();
                payment.setStudent(user);
                payment.setPaidAmount(courseAmount);
                payment.setPaymentStatus(PaymentStatus.PAID);
                payment.setDate(LocalDate.now());
                Payment saved = paymentRepo.save(payment);

                PaymentTransaction paymentTransaction = new PaymentTransaction();
                paymentTransaction.setPaymentMethod(PaymentMethod.valueOf(method));
                paymentTransaction.setAmount(courseAmount);
                paymentTransaction.setTransactionDate(LocalDate.now());
                paymentTransaction.setPayment(saved);
                paymentTransactionRepo.save(paymentTransaction);

                if( distinction > 0 ){

                    int needToPayForNextMonthAmount = courseAmount - distinction;

                    Payment paymentNextMonth = new Payment();
                    paymentNextMonth.setStudent(user);
                    paymentNextMonth.setPaidAmount(distinction);
                    paymentNextMonth.setPaymentStatus(PaymentStatus.PENDING);
                    paymentNextMonth.setDate(LocalDate.now().plusMonths(1));
                    Payment savedNext = paymentRepo.save(paymentNextMonth);

                    PaymentTransaction paymentTransactionForNextPayment = new PaymentTransaction();
                    paymentTransactionForNextPayment.setPaymentMethod(PaymentMethod.valueOf(method));
                    paymentTransactionForNextPayment.setAmount(distinction);
                    paymentTransactionForNextPayment.setTransactionDate(LocalDate.now());
                    paymentTransactionForNextPayment.setPayment(savedNext);
                    paymentTransactionRepo.save(paymentTransactionForNextPayment);

                    Debts debt = new Debts();
                    debt.setAmount(needToPayForNextMonthAmount);
                    debt.setStudent(user);
                    debtsRepo.save(debt);
                }

            }else if(paidAmount.equals(courseAmount)){

                Payment payment1 = new Payment();
                payment1.setStudent(user);
                payment1.setPaidAmount(paidAmount);
                payment1.setPaymentStatus(PaymentStatus.PAID);
                payment1.setDate(LocalDate.now());
                Payment saved1 = paymentRepo.save(payment1);

                PaymentTransaction paymentTrans = new PaymentTransaction();
                paymentTrans.setPaymentMethod(PaymentMethod.valueOf(method));
                paymentTrans.setAmount(paidAmount);
                paymentTrans.setTransactionDate(LocalDate.now());
                paymentTrans.setPayment(saved1);
                paymentTransactionRepo.save(paymentTrans);

            } else {
                int distinction = courseAmount - paidAmount;

                Payment payment = new Payment();
                payment.setStudent(user);
                payment.setPaidAmount(paidAmount);
                payment.setPaymentStatus(PaymentStatus.PENDING);
                payment.setDate(LocalDate.now());
                Payment saved = paymentRepo.save(payment);

                PaymentTransaction paymentTransaction = new PaymentTransaction();
                paymentTransaction.setPaymentMethod(PaymentMethod.valueOf(method));
                paymentTransaction.setAmount(paidAmount);
                paymentTransaction.setTransactionDate(LocalDate.now());
                paymentTransaction.setPayment(saved);
                paymentTransactionRepo.save(paymentTransaction);

                Debts debt = new Debts();
                debt.setAmount(distinction);
                debt.setStudent(user);
                debtsRepo.save(debt);

            }

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

    @Transactional
    @Override
    public List<PaymentResDto> getPaymentsWithTransaction(UUID groupId, String dateFrom, String dateTo, String paymentMethod) {
        List<PaymentResDto> payments = new ArrayList<>();

        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        LocalDate from = LocalDate.parse(dateFrom);
        LocalDate to = LocalDate.parse(dateTo);

        group.getStudents().forEach(student -> {
            List<Payment> paymentList = paymentRepo.findPaymentsByStudent(student);

            paymentList.stream()
                    // Sana oralig‘ida bo‘lgan paymentlarni olish
                    .filter(p -> !p.getDate().isBefore(from) && !p.getDate().isAfter(to))
                    .filter(p -> p.getPaymentTransactions().stream()
                            .anyMatch(t -> paymentMethod == null
                                    || paymentMethod.equalsIgnoreCase("all")
                                    || t.getPaymentMethod().name().equalsIgnoreCase(paymentMethod)))
                    .forEach(payment -> {
                        PaymentResDto dto = new PaymentResDto();
                        dto.setFullName(student.getFirstName() + " " + student.getLastName());
                        dto.setPaymentDate(payment.getDate());
                        dto.setPaidAmount(payment.getPaidAmount());
                        dto.setPaymentStatus(payment.getPaymentStatus().name());

                        // 🔹 Agar "all" bo‘lsa — hech qanday filter yo‘q, hammasi chiqadi
                        List<PaymentTransactionResDto> transactionDtos = payment.getPaymentTransactions().stream()
                                .filter(t -> paymentMethod == null
                                        || paymentMethod.equalsIgnoreCase("all")
                                        || t.getPaymentMethod().name().equalsIgnoreCase(paymentMethod))
                                .map(t -> {
                                    PaymentTransactionResDto tr = new PaymentTransactionResDto();
                                    tr.setPaymentDate(t.getTransactionDate());
                                    tr.setPaidAmount(t.getAmount());
                                    tr.setPaymentMethod(t.getPaymentMethod().name());
                                    return tr;
                                })
                                .toList();

                        dto.setTransactions(transactionDtos);
                        payments.add(dto);
                    });
        });

        return payments;
    }


    @Transactional
    @Override
    public List<PaymentResDto> getUserPaymentsWithTransactions(UUID studentId, String dateFrom, String dateTo, String paymentMethod) {
        User user = userRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate from = LocalDate.parse(dateFrom);
        LocalDate to = LocalDate.parse(dateTo);

        // 🔹 Foydalanuvchining barcha to‘lovlarini olamiz
        List<Payment> payments = paymentRepo.findPaymentsByStudent(user);

        List<PaymentResDto> result = new ArrayList<>();

        for (Payment payment : payments) {
            // Sana oralig‘ida filtrlaymiz
            if (payment.getDate().isBefore(from) || payment.getDate().isAfter(to)) {
                continue;
            }

            boolean hasMatchingTransaction = payment.getPaymentTransactions().stream()
                    .anyMatch(t -> paymentMethod == null
                            || paymentMethod.equalsIgnoreCase("all")
                            || t.getPaymentMethod().name().equalsIgnoreCase(paymentMethod));
            if (!hasMatchingTransaction) {
                continue;
            }

            // 🔹 Har bir Payment uchun yangi DTO
            PaymentResDto dto = new PaymentResDto();
            dto.setFullName(user.getFirstName() + " " + user.getLastName());
            dto.setPaymentDate(payment.getDate());
            dto.setPaidAmount(payment.getPaidAmount());
            dto.setPaymentStatus(payment.getPaymentStatus().name());

            // 🔹 Transaction’larni filtrlaymiz
            List<PaymentTransactionResDto> transactionDtos = payment.getPaymentTransactions().stream()
                    .filter(t -> paymentMethod == null
                            || paymentMethod.equalsIgnoreCase("all")
                            || t.getPaymentMethod().name().equalsIgnoreCase(paymentMethod))

                    .map(t -> {
                        PaymentTransactionResDto tr = new PaymentTransactionResDto();
                        tr.setPaymentDate(t.getTransactionDate());
                        tr.setPaidAmount(t.getAmount());
                        tr.setPaymentMethod(t.getPaymentMethod().name());
                        return tr;
                    })
                    .toList();

            dto.setTransactions(transactionDtos);
            result.add(dto);
        }

        return result;
    }


}
