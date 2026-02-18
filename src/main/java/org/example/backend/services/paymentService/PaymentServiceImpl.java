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
import java.util.*;


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

        Discount discount = discountRepo
                .findByStudentAndActiveTrue(user)
                .orElse(null);


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

        payment.getPaymentTransactions().add(pt);

        paymentRepo.save(payment); // cascade ALL bor
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
                                dto.setId(payment.getId());
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

                                            // MANA SHU QATORNI QO'SHING:
                                            tr.setId(t.getId());
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
            dto.setId(payment.getId());
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
                        tr.setId(t.getId());
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


            Discount discountEntity = discountRepo
                    .findByStudentAndActiveTrue(user)
                    .orElse(null);

            Integer discount = (discountEntity != null) ? discountEntity.getQuantity() : 0;
            dto.setDiscountAmount(discount);

            dto.setPaymentStatus(payment.getPaymentStatus().name());

            List<PaymentTransactionResDto> transactionResDtoList = new ArrayList<>();

            payment.getPaymentTransactions().forEach(pt -> {
                PaymentTransactionResDto tr = new PaymentTransactionResDto();
                tr.setId(pt.getId());
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

        Discount byStudent = discountRepo
                .findByStudentAndActiveTrue(user)
                .orElse(null);


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

//    @Transactional
//    @Override
//    public void deletePayment(UUID paymentId) {
//
//        Payment payment = paymentRepo.findById(paymentId)
//                .orElseThrow(() -> new RuntimeException("Payment not found"));
//
//        User student = payment.getStudent();
//        LocalDate paymentDate = payment.getDate();
//
//        int paidAmount = payment.getPaidAmount() != null ? payment.getPaidAmount() : 0;
//
//        // ==========================
//        // TRANSACTION LARNI O‘CHIRISH
//        // ==========================
//        List<PaymentTransaction> transactions =
//                paymentTransactionRepo.findByPayment(payment);
//
//        paymentTransactionRepo.deleteAll(transactions);
//
//        // ==========================
//        // QARZNI TO‘G‘RI HISOBLASH
//        // ==========================
//        PaymentCourseInfo pci = paymentCourseInfoRepo.findAll()
//                .stream().findFirst()
//                .orElseThrow(() -> new RuntimeException("Course info not found"));
//
//        int courseAmount = pci.getCoursePaymentAmount();
//        int discountAmount = payment.getDiscountAmount() != null
//                ? payment.getDiscountAmount()
//                : 0;
//
//        int realAmount = Math.max(courseAmount - discountAmount, 0);
//
//        int debtAmount = realAmount - paidAmount;
//
//        if (realAmount > 0) {
//
//            Optional<Debts> existingDebt =
//                    debtsRepo.findByStudentAndCreatedDate(student, paymentDate);
//
//            if (existingDebt.isPresent()) {
//                Debts debt = existingDebt.get();
//                debt.setAmount(realAmount); // 🔥 qo‘shmaymiz, to‘liq almashtiramiz
//                debtsRepo.save(debt);
//            } else {
//                Debts debt = new Debts();
//                debt.setStudent(student);
//                debt.setAmount(realAmount);
//                debt.setCreatedDate(paymentDate);
//                debtsRepo.save(debt);
//            }
//        }
//
//
//        // ==========================
//        // PAYMENT NI O‘CHIRISH
//        // ==========================
//        paymentRepo.delete(payment);
//    }

    @Transactional
    @Override
    public void deletePayment(UUID paymentId) {

        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        User student = payment.getStudent();
        LocalDate paymentDate = payment.getDate();

        // 1️⃣ Transactionlarni o‘chiramiz
        List<PaymentTransaction> transactions =
                paymentTransactionRepo.findByPayment(payment);

        paymentTransactionRepo.deleteAll(transactions);

        // 2️⃣ Shu sanaga tegishli debtni o‘chiramiz
        debtsRepo.findByStudentAndCreatedDate(student, paymentDate)
                .ifPresent(debtsRepo::delete);

        // 3️⃣ Paymentni o‘chiramiz
        paymentRepo.delete(payment);
    }


//    @Transactional
//    @Override
//    public void deletePaymentTransaction(UUID transactionId) {
//
//        // ==========================
//        // TRANSACTION TOPISH
//        // ==========================
//        PaymentTransaction transaction = paymentTransactionRepo.findById(transactionId)
//                .orElseThrow(() -> new RuntimeException("Payment transaction not found"));
//
//        Payment payment = transaction.getPayment();
//        User student = payment.getStudent();
//        LocalDate paymentDate = payment.getDate();
//
//        // ==========================
//        // TRANSACTION NI O‘CHIRISH
//        // ==========================
//        paymentTransactionRepo.delete(transaction);
//        paymentTransactionRepo.flush();
//
//        // ==========================
//        // QOLGAN TRANSACTIONLARNI OLISH
//        // ==========================
//        List<PaymentTransaction> remainingTransactions =
//                paymentTransactionRepo.findByPayment(payment);
//
//        // ==========================
//        // AGAR TRANSACTION QOLMAGAN BO‘LSA
//        // ==========================
//        if (remainingTransactions.isEmpty()) {
//
//            // Debt mavjud bo‘lsa o‘chiramiz
//            debtsRepo.findByStudentAndCreatedDate(student, paymentDate)
//                    .ifPresent(debtsRepo::delete);
//
//            // Paymentni ham o‘chiramiz
//            paymentRepo.delete(payment);
//
//            return; // method tugaydi
//        }
//
//        // ==========================
//        // AKS HOLDA PAID NI QAYTA HISOBLAYMIZ
//        // ==========================
//        int newPaidAmount = remainingTransactions.stream()
//                .mapToInt(PaymentTransaction::getAmount)
//                .sum();
//
//        payment.setPaidAmount(newPaidAmount);
//
//        // ==========================
//        // COURSE AMOUNT
//        // ==========================
//        PaymentCourseInfo pci = paymentCourseInfoRepo.findAll()
//                .stream()
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("Course info not found"));
//
//        int courseAmount = pci.getCoursePaymentAmount();
//        int discountAmount = payment.getDiscountAmount() != null
//                ? payment.getDiscountAmount()
//                : 0;
//
//        int realAmount = Math.max(courseAmount - discountAmount, 0);
//
//        // ==========================
//        // STATUS UPDATE
//        // ==========================
//        if (newPaidAmount >= realAmount) {
//            payment.setPaymentStatus(PaymentStatus.PAID);
//        } else {
//            payment.setPaymentStatus(PaymentStatus.PENDING);
//        }
//
//        paymentRepo.save(payment);
//
//        // ==========================
//        // DEBT QAYTA HISOBLASH
//
//        // ==========================
//        int debtAmount = realAmount - newPaidAmount;
//
//        Optional<Debts> existingDebt =
//                debtsRepo.findByStudentAndCreatedDate(student, paymentDate);
//
//        if (debtAmount > 0) {
//            if (existingDebt.isPresent()) {
//                existingDebt.get().setAmount(debtAmount);
//            } else {
//                Debts debt = new Debts();
//                debt.setStudent(student);
//                debt.setAmount(debtAmount);
//                debt.setCreatedDate(paymentDate);
//                debtsRepo.save(debt);
//            }
//        } else {
//            existingDebt.ifPresent(debtsRepo::delete);
//        }
//    }

    @Transactional
    @Override
    public void deletePaymentTransaction(UUID transactionId) {

        PaymentTransaction transaction = paymentTransactionRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found"));

        Payment payment = transaction.getPayment();
        User student = payment.getStudent();
        LocalDate paymentDate = payment.getDate();

        paymentTransactionRepo.delete(transaction);
        paymentTransactionRepo.flush();

        List<PaymentTransaction> remaining =
                paymentTransactionRepo.findByPayment(payment);

        if (remaining.isEmpty()) {

            debtsRepo.findByStudentAndCreatedDate(student, paymentDate)
                    .ifPresent(debtsRepo::delete);

            paymentRepo.delete(payment);
            return;
        }

        int newPaidAmount = remaining.stream()
                .mapToInt(PaymentTransaction::getAmount)
                .sum();

        payment.setPaidAmount(newPaidAmount);

        PaymentCourseInfo pci = paymentCourseInfoRepo.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Course info not found"));

        int realAmount = pci.getCoursePaymentAmount()
                - (payment.getDiscountAmount() != null ? payment.getDiscountAmount() : 0);

        int debtAmount = realAmount - newPaidAmount;

        if (debtAmount > 0) {

            Debts debt = debtsRepo
                    .findByStudentAndCreatedDate(student, paymentDate)
                    .orElseGet(() -> {
                        Debts d = new Debts();
                        d.setStudent(student);
                        d.setCreatedDate(paymentDate);
                        return d;
                    });

            debt.setAmount(debtAmount);
            debtsRepo.save(debt);

            payment.setPaymentStatus(PaymentStatus.PENDING);

        } else {

            debtsRepo.findByStudentAndCreatedDate(student, paymentDate)
                    .ifPresent(debtsRepo::delete);

            payment.setPaymentStatus(PaymentStatus.PAID);
        }

        paymentRepo.save(payment);
    }


}
