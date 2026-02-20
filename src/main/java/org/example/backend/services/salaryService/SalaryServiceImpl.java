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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalaryServiceImpl implements SalaryService {

    private final TeacherSalaryRepo teacherSalaryRepo;
    private final ReceptionSalaryRepo receptionSalaryRepo;
    private final UserRepo userRepo;
    private final TeacherSalaryPaymentRepo teacherSalaryPaymentRepo;
    private final PaymentCourseInfoRepo paymentCourseInfoRepo;
    private final ReceptionSalaryPaymentRepo receptionSalaryPaymentRepo;
    private final GroupRepo groupRepo;


    @Override
    @Transactional
    public List<?> getSalaries(String filialId, String role, Integer year, Integer month) {

        // ⚠ Agar month 0-based kelmasa (1-12 bo‘lsa) +1 ni olib tashlang
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

                List<TeacherSalary> salaries =
                        teacherSalaryRepo.findAllByTeacherAndMonth(
                                teacher.getId(), startDate, endDate
                        );

                TeacherSalary salary;

                if (!salaries.isEmpty()) {
                    salary = salaries.get(0);
                } else {

                    salary = new TeacherSalary();
                    salary.setTeacher(teacher);
                    salary.setSalaryDate(startDate);

                    // 🔥 O‘tgan oyning ma’lumotlarini ko‘chiramiz
                    Optional<TeacherSalary> lastSalary =
                            teacherSalaryRepo
                                    .findTopByTeacherIdAndSalaryDateBeforeOrderBySalaryDateDesc(
                                            teacher.getId(),
                                            startDate
                                    );

                    salary.setTotalAmount(
                            lastSalary.map(TeacherSalary::getTotalAmount).orElse(0)
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

                // 🔥 Group nomlarini paymentlardan yig‘amiz
                Set<String> groupNames = salary.getPayments() == null
                        ? Collections.emptySet()
                        : salary.getPayments().stream()
                        .map(TeacherSalaryPayment::getGroup)
                        .filter(Objects::nonNull)
                        .map(Group::getName)
                        .collect(Collectors.toSet());

                dto.setGroupNames(new ArrayList<>(groupNames));

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

                    salary = new ReceptionSalary();
                    salary.setReceptionist(reception);
                    salary.setSalaryDate(startDate);

                    Optional<ReceptionSalary> lastSalary =
                            receptionSalaryRepo
                                    .findTopByReceptionistIdAndSalaryDateBeforeOrderBySalaryDateDesc(
                                            reception.getId(),
                                            startDate
                                    );

                    salary.setSalaryAmount(
                            lastSalary.map(ReceptionSalary::getSalaryAmount).orElse(0)
                    );

                    salary = receptionSalaryRepo.save(salary);
                }

                SalaryReceptionRes dto = new SalaryReceptionRes();
                dto.setId(salary.getId());
                dto.setFullName(
                        reception.getFirstName() + " " + reception.getLastName()
                );
                dto.setSalaryAmount(salary.getSalaryAmount());

                double paidAmount = salary.getPayments() == null
                        ? 0
                        : salary.getPayments().stream()
                        .mapToDouble(ReceptionSalaryPayment::getAmount)
                        .sum();

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

        TeacherSalary salary = teacherSalaryRepo.findById(salaryId)
                .orElseThrow(() -> new RuntimeException("Salary not found"));

        return salary.getPayments().stream()
                .map(p -> {
                    SalaryPaymentResDto dto = new SalaryPaymentResDto();
                    dto.setId(p.getId());
                    dto.setDate(p.getPaymentDate());
                    dto.setAmount(p.getAmount());

                    String groupName = p.getGroup() != null
                            ? p.getGroup().getName()
                            : null;

                    dto.setGroupName(groupName);

                    return dto;
                })
                .toList();
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

        PaymentCourseInfo paymentInfo = paymentCourseInfoRepo.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Course payment info not found"));

        Integer coursePaymentAmount = paymentInfo.getCoursePaymentAmount();

        List<SalaryByGroupInfoResDto> result = new ArrayList<>();

        for (TeacherSalary salary : salaries) {

            Integer percentage = salary.getPercentage() != null
                    ? salary.getPercentage()
                    : 0;

            // 🔥 Salary ichidagi barcha grouplarni aniqlaymiz
            Set<Group> groups = salary.getPayments().stream()
                    .map(TeacherSalaryPayment::getGroup)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            for (Group group : groups) {

                // 🔥 Paid amount hisoblash
                Integer paidAmount = salary.getPayments().stream()
                        .filter(p -> group.equals(p.getGroup()))
                        .map(TeacherSalaryPayment::getAmount)
                        .reduce(0, Integer::sum);

                // 🔥 Active studentlar soni
                int numStudents = (int) group.getGroupStudents().stream()
                        .filter(gs -> gs.getStatus() == GroupStudentStatus.ACTIVE)
                        .count();

                int perStudentAmount = (coursePaymentAmount * percentage) / 100;
                Integer mustPaid = perStudentAmount * numStudents;

                SalaryByGroupInfoResDto dto = new SalaryByGroupInfoResDto();
                dto.setId(salary.getId());
                dto.setGroupName(group.getName());
                dto.setPercentage(percentage);
                dto.setMustPaid(mustPaid);
                dto.setAmount(paidAmount);
                dto.setDate(salary.getSalaryDate());

                result.add(dto);
            }
        }

        return result;
    }


    @Transactional
    @Override
    public void addSalaryPayment(UUID salaryId, UUID groupId, Integer amount) {

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        TeacherSalary salary = teacherSalaryRepo.findById(salaryId)
                .orElseThrow(() -> new RuntimeException("Teacher salary not found"));

        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        TeacherSalaryPayment payment = new TeacherSalaryPayment();
        payment.setPaymentDate(LocalDate.now());
        payment.setAmount(amount);
        payment.setTeacherSalary(salary);
        payment.setGroup(group); // 🔥 endi to‘g‘ri

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
