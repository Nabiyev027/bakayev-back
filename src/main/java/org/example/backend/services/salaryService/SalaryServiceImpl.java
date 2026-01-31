package org.example.backend.services.salaryService;

import lombok.RequiredArgsConstructor;
import org.example.backend.Enum.GroupStudentStatus;
import org.example.backend.dtoResponse.SalaryByGroupInfoResDto;
import org.example.backend.dtoResponse.SalaryPaymentResDto;
import org.example.backend.dtoResponse.SalaryReceptionRes;
import org.example.backend.dtoResponse.SalaryTeacherRes;
import org.example.backend.entity.*;
import org.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SalaryServiceImpl implements SalaryService {

    private final TeacherSalaryRepo teacherSalaryRepo;
    private final ReceptionSalaryRepo receptionSalaryRepo;
    private final UserRepo userRepo;
    private final TeacherSalaryPaymentRepo teacherSalaryPaymentRepo;
    private final PaymentCourseInfoRepo paymentCourseInfoRepo;
    private final ReceptionSalaryPaymentRepo receptionSalaryPaymentRepo;


    @Override
    @Transactional
    public List<?> getSalaries(String filialId, String role, Integer year, Integer month) {

        LocalDate startDate = LocalDate.of(year, month + 1, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Object> result = new ArrayList<>();

        // ====================== TEACHER ======================
        if ("ROLE_TEACHER".equals(role)) {

            List<User> teachers;

            if ("all".equals(filialId)) {
                teachers = userRepo.findAllByRole("ROLE_TEACHER");
            } else {
                UUID filialUUID = UUID.fromString(filialId);
                teachers = userRepo.findAllByRoleAndFilial("ROLE_TEACHER", filialUUID);
            }

            for (User teacher : teachers) {

                Optional<TeacherSalary> optionalSalary =
                        teacherSalaryRepo.findByTeacherAndMonth(
                                teacher.getId(), startDate, endDate
                        );

                TeacherSalary salary;

                if (optionalSalary.isPresent()) {
                    salary = optionalSalary.get();
                } else {
                    salary = new TeacherSalary();
                    salary.setTeacher(teacher);

                    if (teacher.getTeacherGroups() != null && !teacher.getTeacherGroups().isEmpty()) {
                        salary.setGroup(teacher.getTeacherGroups().get(0));
                    } else {
                        salary.setGroup(null);
                    }

                    salary.setTotalAmount(0);
                    salary.setSalaryDate(startDate);

                    // ⭐ O‘TGAN OY PERCENTAGE NI OLAMIZ
                    Optional<TeacherSalary> lastSalary =
                            teacherSalaryRepo
                                    .findTopByTeacherIdAndSalaryDateBeforeOrderBySalaryDateDesc(
                                            teacher.getId(),
                                            startDate
                                    );

                    salary.setPercentage(
                            lastSalary.map(TeacherSalary::getPercentage).orElse(0)
                    );

                    salary = teacherSalaryRepo.save(salary);
                }


                SalaryTeacherRes dto = new SalaryTeacherRes();
                dto.setId(salary.getId());
                dto.setTeacherId(teacher.getId());
                dto.setFullName(
                        teacher.getFirstName() + " " + teacher.getLastName()
                );
                dto.setDate(salary.getSalaryDate());

                // 👇 Guruh nomini frontendga jo‘natamiz
                if (salary.getGroup() != null) {
                    dto.setGroupNames(List.of(salary.getGroup().getName()));
                } else {
                    dto.setGroupNames(Collections.emptyList());
                }

                result.add(dto);
            }

            return result;
        }

        // ====================== RECEPTION ======================
        if ("ROLE_RECEPTION".equals(role)) {

            List<User> receptions;

            if ("all".equals(filialId)) {
                receptions = userRepo.findAllByRole("ROLE_RECEPTION");
            } else {
                UUID filialUUID = UUID.fromString(filialId);
                receptions = userRepo.findAllByRoleAndFilial("ROLE_RECEPTION", filialUUID);
            }

            for (User reception : receptions) {

                Optional<ReceptionSalary> optionalSalary =
                        receptionSalaryRepo.findByReceptionAndMonth(
                                reception.getId(), startDate, endDate
                        );

                ReceptionSalary salary;

                if (optionalSalary.isPresent()) {
                    salary = optionalSalary.get();
                } else {
                    // 🔥 Salary yo‘q → 0 bilan yaratamiz
                    salary = new ReceptionSalary();
                    salary.setReceptionist(reception);
                    salary.setSalaryAmount(0);
                    salary.setSalaryDate(startDate);
                    salary = receptionSalaryRepo.save(salary);
                }

                SalaryReceptionRes dto = new SalaryReceptionRes();
                dto.setId(salary.getId());
                dto.setFullName(
                        reception.getFirstName() + " " + reception.getLastName()
                );
                dto.setSalaryAmount(salary.getSalaryAmount());

                double paidAmount = 0;
                if (salary.getPayments() != null && !salary.getPayments().isEmpty()) {
                    paidAmount = salary.getPayments().stream()
                            .mapToDouble(ReceptionSalaryPayment::getAmount)
                            .sum();
                }
                dto.setPaidAmount(paidAmount);
                dto.setDate(salary.getSalaryDate());

                result.add(dto);
            }

            return result;
        }

        return Collections.emptyList();
    }



    @Transactional
    @Override
    public List<SalaryPaymentResDto> getSalPayments(UUID salaryId) {
        List<SalaryPaymentResDto> payments = new ArrayList<>();

        TeacherSalary salary = teacherSalaryRepo.findById(salaryId)
                .orElseThrow(() -> new RuntimeException("Salary not found"));

        String groupName = salary.getGroup() != null
                ? salary.getGroup().getName()
                : null; // yoki "Noma'lum guruh"

        for (TeacherSalaryPayment p : salary.getPayments()) {
            SalaryPaymentResDto dto = new SalaryPaymentResDto();
            dto.setId(p.getId());
            dto.setDate(p.getPaymentDate());
            dto.setAmount(p.getAmount());
            dto.setGroupName(groupName);
            payments.add(dto);
        }

        return payments;
    }


    @Transactional
    @Override
    public List<SalaryByGroupInfoResDto> getSalaryGroupInfo(
            UUID teacherId,
            Integer year,
            Integer month
    ) {

        User teacher = userRepo.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        LocalDate startDate = LocalDate.of(year, month + 1, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<TeacherSalary> salaries =
                teacherSalaryRepo.findAllByTeacherAndSalaryDateBetween(
                        teacher,
                        startDate,
                        endDate
                );

        List<SalaryByGroupInfoResDto> result = new ArrayList<>();

        PaymentCourseInfo paymentInfo = paymentCourseInfoRepo.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Course payment info not found"));

        Integer coursePaymentAmount = paymentInfo.getCoursePaymentAmount();

        for (TeacherSalary salary : salaries) {

            Group group = salary.getGroup();

            // 🔒 AGAR GROUP YO‘Q BO‘LSA → O‘TKAZIB YUBORAMIZ
            if (group == null) {
                SalaryByGroupInfoResDto dto = new SalaryByGroupInfoResDto();
                dto.setId(salary.getId());
                dto.setGroupName(null); // yoki "Guruh yo‘q"
                dto.setMustPaid(0);
                dto.setPercentage(salary.getPercentage());
                dto.setAmount(0);
                dto.setDate(salary.getSalaryDate());
                result.add(dto);
                continue;
            }

            Integer paidAmount =
                    teacherSalaryPaymentRepo
                            .sumAmountByTeacherSalaryAndGroup(salary, group);

            if (paidAmount == null) {
                paidAmount = 0;
            }

            Integer percentage = salary.getPercentage();
            if (percentage == null) {
                percentage = 0; // yoki 100
            }

            int numStudents = (int) group.getGroupStudents().stream()
                    .filter(gs -> gs.getStatus() == GroupStudentStatus.ACTIVE)
                    .count();

// 1 studentdan olinadigan summa
            int perStudentAmount = (coursePaymentAmount * percentage) / 100;

// O‘qituvchining umumiy olishi kerak bo‘lgan summa
            Integer mustPaid = perStudentAmount * numStudents;

            SalaryByGroupInfoResDto dto = new SalaryByGroupInfoResDto();
            dto.setMustPaid(mustPaid);
            dto.setId(salary.getId());
            dto.setGroupName(group.getName());
            dto.setPercentage(salary.getPercentage());
            dto.setAmount(paidAmount);
            dto.setDate(salary.getSalaryDate());

            result.add(dto);
        }

        return result;
    }



    @Transactional
    @Override
    public void addSalaryPayment(UUID salaryId,UUID groupId,Integer amount) {

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        TeacherSalary salary = teacherSalaryRepo.findById(salaryId)
                .orElseThrow(() -> new RuntimeException("Teacher salary not found"));

        TeacherSalaryPayment payment = new TeacherSalaryPayment();
        payment.setPaymentDate(LocalDate.now());
        payment.setAmount(amount);
        payment.setTeacherSalary(salary);
        teacherSalaryPaymentRepo.save(payment);
    }

    @Transactional
    @Override
    public void deleteSalaryPayment(UUID paymentId) {

        TeacherSalaryPayment payment = teacherSalaryPaymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Salary payment not found"));

        teacherSalaryPaymentRepo.delete(payment);
    }

    @Override
    public void updatePercentage(UUID salaryId, Integer percentage) {
        TeacherSalary salary = teacherSalaryRepo.findById(salaryId).orElseThrow(() -> new RuntimeException("Teacher salary not found"));
        salary.setPercentage(percentage);
        teacherSalaryRepo.save(salary);
    }

    @Override
    @Transactional
    public void updateReceptionAmount(UUID salaryId, Integer amount) {

        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("Salary amount noto‘g‘ri");
        }

        ReceptionSalary receptionSalary = receptionSalaryRepo.findById(salaryId)
                .orElseThrow(() -> new RuntimeException("Reception salary topilmadi"));

        receptionSalary.setSalaryAmount(amount);
    }

    @Override
    public void deleteRecSalaryPayment(UUID paymentId) {
        ReceptionSalaryPayment payment = receptionSalaryPaymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Salary payment not found"));

        receptionSalaryPaymentRepo.delete(payment);
    }

    @Override
    @Transactional
    public List<SalaryPaymentResDto> getSalRecPayments(UUID salaryId) {
        List<SalaryPaymentResDto> payments = new ArrayList<>();

        ReceptionSalary salary = receptionSalaryRepo.findById(salaryId)
                .orElseThrow(() -> new RuntimeException("Salary not found"));

        for (ReceptionSalaryPayment p : salary.getPayments()) {
            SalaryPaymentResDto dto = new SalaryPaymentResDto();
            dto.setId(p.getId());
            dto.setDate(p.getPaymentDate());
            dto.setAmount(p.getAmount());
            payments.add(dto);
        }

        return payments;
    }

    @Override
    @Transactional
    public void addRecSalaryPayment(UUID salaryId, Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        ReceptionSalary salary = receptionSalaryRepo.findById(salaryId)
                .orElseThrow(() -> new RuntimeException("Teacher salary not found"));

        ReceptionSalaryPayment payment = new ReceptionSalaryPayment();
        payment.setPaymentDate(LocalDate.now());
        payment.setAmount(amount);
        payment.setReceptionSalary(salary);

        receptionSalaryPaymentRepo.save(payment);
    }


}
