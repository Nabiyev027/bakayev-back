package org.example.backend.services.paymentService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.PaymentMethod;
import org.example.backend.Enum.PaymentStatus;
import org.example.backend.dto.PaymentDto;
import org.example.backend.dtoResponse.PaymentAmountResDto;
import org.example.backend.dtoResponse.PaymentInfoResDto;
import org.example.backend.dtoResponse.PaymentResDto;
import org.example.backend.dtoResponse.PaymentTransactionResDto;
import org.example.backend.entity.*;
import org.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private final DiscountRepo discountRepo;
    

    @Transactional
    @Override
    public void addPayment(PaymentDto paymentDto) {

        User user = userRepo.findById(paymentDto.getStudentId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PaymentCourseInfo pci = paymentCourseInfoRepo.findAll()
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Course info not found"));

        int courseAmount = pci.getCoursePaymentAmount();
        int remainingAmount = paymentDto.getAmount();

        Discount discount = discountRepo.findByStudent(user);

        String method = paymentDto.getPaymentMethod().equalsIgnoreCase("CARD")
                ? PaymentMethod.CARD.name()
                : PaymentMethod.CASH.name();

        // ==========================
        // OLD DEBTS NI TO‘LDIRISH
        // ==========================
        List<Debts> debts = debtsRepo.findByStudentOrderByCreatedDateAsc(user);

        for (Debts debt : debts) {
            if (remainingAmount <= 0) break;

            int debtAmount = debt.getAmount();
            LocalDate debtMonth = debt.getCreatedDate();

            Payment payment = paymentRepo.findByStudentAndDate(user, debtMonth)
                    .orElseGet(() -> {
                        Payment p = new Payment();
                        p.setStudent(user);
                        p.setDate(debtMonth);
                        p.setPaidAmount(0);
                        p.setDiscountAmount(0);
                        p.setPaymentStatus(PaymentStatus.PENDING);
                        return p;
                    });

            int payAmount = Math.min(remainingAmount, debtAmount);
            remainingAmount -= payAmount;

            payment.setPaidAmount(payment.getPaidAmount() + payAmount);

            int paymentDiscount = payment.getDiscountAmount() != null
                    ? payment.getDiscountAmount()
                    : 0;

            int realCourseAmount = Math.max(courseAmount - paymentDiscount, 0);

            payment.setPaymentStatus(
                    payment.getPaidAmount() >= realCourseAmount
                            ? PaymentStatus.PAID
                            : PaymentStatus.PENDING
            );

            paymentRepo.save(payment);
            saveTransaction(payment, payAmount, method);

            if (payAmount >= debtAmount) {
                debtsRepo.delete(debt);
            } else {
                debt.setAmount(debtAmount - payAmount);
                debtsRepo.save(debt);
            }
        }

        // ==========================
        // OXIRGI PAYMENT SANASINI ANIQLASH
        // ==========================
        LocalDate currentMonthDate = paymentRepo
                .findTopByStudentOrderByDateDesc(user)
                .map(p -> p.getDate().plusMonths(1))
                .orElseGet(() -> {
                    LocalDate now = LocalDate.now();
                    return now.withDayOfMonth(
                            Math.min(pci.getPaymentDay(), now.lengthOfMonth())
                    );
                });

        // ==========================
        // YANGI OYLAR UCHUN PAYMENT
        // ==========================
        while (remainingAmount > 0) {

            int discountAmount = 0;
            if (discount != null &&
                    (discount.getEndDate() == null || !discount.getEndDate().isBefore(currentMonthDate))) {
                discountAmount = discount.getQuantity();
            }

            int realCourseAmount = Math.max(courseAmount - discountAmount, 0);
            int payThisMonth = Math.min(realCourseAmount, remainingAmount);

            Payment payment = new Payment();
            payment.setStudent(user);
            payment.setDate(currentMonthDate);
            payment.setPaidAmount(payThisMonth);
            payment.setDiscountAmount(discountAmount);
            payment.setPaymentStatus(
                    payThisMonth >= realCourseAmount
                            ? PaymentStatus.PAID
                            : PaymentStatus.PENDING
            );

            paymentRepo.save(payment);
            saveTransaction(payment, payThisMonth, method);

            if (payThisMonth < realCourseAmount) {
                Debts d = new Debts();
                d.setStudent(user);
                d.setAmount(realCourseAmount - payThisMonth);
                d.setCreatedDate(currentMonthDate);
                debtsRepo.save(d);
            }

            remainingAmount -= payThisMonth;

            // 🔥 MUHIM: har doim oxirgi payment sanasidan keyingi oy
            currentMonthDate = currentMonthDate.plusMonths(1);
            currentMonthDate = currentMonthDate.withDayOfMonth(
                    Math.min(pci.getPaymentDay(), currentMonthDate.lengthOfMonth())
            );
        }
    }

    private void saveTransaction(Payment payment, int amount, String method) {
        PaymentTransaction pt = new PaymentTransaction();
        pt.setPayment(payment);
        pt.setAmount(amount);
        pt.setPaymentMethod(PaymentMethod.valueOf(method));
        pt.setTransactionDate(LocalDate.now());
        paymentTransactionRepo.save(pt);
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

        // 🔹 GroupStudent orqali studentlarni olish
        group.getGroupStudents().stream()
                .map(GroupStudent::getStudent)
                .forEach(student -> {
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
                                dto.setDiscountAmount(payment.getDiscountAmount());

                                dto.setPaymentStatus(payment.getPaymentStatus().name());

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
            dto.setDiscountAmount(payment.getDiscountAmount());
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

    @Transactional
    @Override
    public List<PaymentResDto> getPayments(UUID id) {
        List<PaymentResDto> payments = new ArrayList<>();

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Payment> paymentsByStudent = paymentRepo.findPaymentsByStudent(user);

        paymentsByStudent.forEach(payment -> {
            PaymentResDto dto = new PaymentResDto();
            dto.setId(payment.getId());
            dto.setFullName(user.getFirstName() + " " + user.getLastName());
            dto.setPaymentDate(payment.getDate());
            dto.setPaidAmount(payment.getPaidAmount());

            // ❗ Null CHECK QO‘YILDI
            Discount discountEntity = discountRepo.findByStudent(user);
            Integer discount = (discountEntity != null) ? discountEntity.getQuantity() : 0;
            dto.setDiscountAmount(discount);

            dto.setPaymentStatus(payment.getPaymentStatus().name());

            List<PaymentTransactionResDto> transactionResDtoList = new ArrayList<>();

            payment.getPaymentTransactions().forEach(pt -> {
                PaymentTransactionResDto tr = new PaymentTransactionResDto();
                tr.setPaymentMethod(pt.getPaymentMethod().name());
                tr.setPaidAmount(pt.getAmount());
                tr.setPaymentDate(pt.getTransactionDate());
                transactionResDtoList.add(tr);
            });

            dto.setTransactions(transactionResDtoList);
            payments.add(dto);
        });

        return payments;
    }


    @Transactional
    @Override
    public Integer getPaymentInfo(UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Discount byStudent = discountRepo.findByStudent(user);

        PaymentCourseInfo paymentPrice = paymentCourseInfoRepo.findAll().getFirst();

        int discount = (byStudent != null) ? byStudent.getQuantity() : 0;

        return paymentPrice.getCoursePaymentAmount() - discount;
    }

    @Transactional
    @Override
    public List<PaymentAmountResDto> getPaymentAmounts(String id) {
        List<PaymentAmountResDto> result = new ArrayList<>();

        List<Payment> allPayments = new ArrayList<>();

        // Paymentlarni olish
        if (id.equals("all")) {
            allPayments = paymentRepo.findAll();
        } else {
            UUID userId = UUID.fromString(id);
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Filial filial = user.getFilials().get(0); // birinchi filial
            List<User> students = userRepo.findStudentsByFilial(filial);

            for (User st : students) {
                List<Payment> paymentsByStudent = paymentRepo.findPaymentsByStudent(st);
                allPayments.addAll(paymentsByStudent);
            }
        }


        // Bugungi sana va hafta
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        int week = today.get(WeekFields.of(Locale.getDefault()).weekOfYear());

        // Yearly
        int yearlySum = allPayments.stream()
                .filter(p -> p.getDate() != null && p.getDate().getYear() == year)
                .mapToInt(Payment::getPaidAmount)
                .sum();
        long yearlyCount = allPayments.stream()
                .filter(p -> p.getDate() != null && p.getDate().getYear() == year)
                .count();
        result.add(new PaymentAmountResDto((int) yearlyCount, yearlySum));

        // Monthly
        int monthlySum = allPayments.stream()
                .filter(p -> p.getDate() != null && p.getDate().getYear() == year && p.getDate().getMonthValue() == month)
                .mapToInt(Payment::getPaidAmount)
                .sum();
        long monthlyCount = allPayments.stream()
                .filter(p -> p.getDate() != null && p.getDate().getYear() == year && p.getDate().getMonthValue() == month)
                .count();
        result.add(new PaymentAmountResDto((int) monthlyCount, monthlySum));

        // Weekly
        int weeklySum = allPayments.stream()
                .filter(p -> p.getDate() != null &&
                        p.getDate().get(WeekFields.of(Locale.getDefault()).weekOfYear()) == week &&
                        p.getDate().getYear() == year)
                .mapToInt(Payment::getPaidAmount)
                .sum();
        long weeklyCount = allPayments.stream()
                .filter(p -> p.getDate() != null &&
                        p.getDate().get(WeekFields.of(Locale.getDefault()).weekOfYear()) == week &&
                        p.getDate().getYear() == year)
                .count();
        result.add(new PaymentAmountResDto((int) weeklyCount, weeklySum));

        // Daily
        int dailySum = allPayments.stream()
                .filter(p -> p.getDate() != null && p.getDate().isEqual(today))
                .mapToInt(Payment::getPaidAmount)
                .sum();
        long dailyCount = allPayments.stream()
                .filter(p -> p.getDate() != null && p.getDate().isEqual(today))
                .count();
        result.add(new PaymentAmountResDto((int) dailyCount, dailySum));

        return result;
    }


}
