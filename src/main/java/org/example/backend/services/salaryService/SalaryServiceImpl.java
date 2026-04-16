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

        int realMonth = month + 1;

        LocalDate startDate = LocalDate.of(year, realMonth, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        LocalDate now = LocalDate.now();
        boolean isCurrentMonth =
                now.getYear() == year &&
                        now.getMonthValue() == realMonth;

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
                        teacherSalaryRepo.findByTeacherAndDateRange(
                                teacher.getId(),
                                startDate,
                                endDate
                        );

                // Agar joriy oy bo'lsa va ustozning guruhlariga salary yaratilmagan bo'lsa, yaratamiz
                if (isCurrentMonth) {
                    List<Group> teacherGroups = teacher.getTeacherGroups();
                    if (teacherGroups != null) {
                        for (Group group : teacherGroups) {
                            boolean hasSalaryForGroup = salaries.stream()
                                    .anyMatch(s -> s.getGroup().getId().equals(group.getId()));

                            if (!hasSalaryForGroup) {
                                TeacherSalary newSalary = new TeacherSalary();
                                newSalary.setTeacher(teacher);
                                newSalary.setGroup(group);
                                newSalary.setSalaryDate(startDate);
                                newSalary.setPercentage(0);
                                newSalary.setTotalAmount(0);
                                salaries.add(teacherSalaryRepo.save(newSalary));
                            }
                        }
                    }
                } else if (salaries.isEmpty()) {
                    continue;
                }

                // DTO ga ma'lumotlarni yig'amiz (ustozning barcha guruhlari bo'yicha jami summalarni)
                SalaryTeacherRes dto = new SalaryTeacherRes();
                dto.setId(teacher.getId()); // Frontend uchun teacherId ni berib yuboramiz
                dto.setTeacherId(teacher.getId());
                dto.setFullName(teacher.getFirstName() + " " + teacher.getLastName());
                dto.setDate(startDate);

                int totalPaidAmount = 0;
                Set<String> groupNames = new HashSet<>();

                for (TeacherSalary ts : salaries) {
                    groupNames.add(ts.getGroup().getName());
                    
                    if (ts.getPayments() != null) {
                        totalPaidAmount += ts.getPayments().stream()
                                .mapToInt(TeacherSalaryPayment::getAmount)
                                .sum();
                    }
                }

                dto.setTotalAmount(totalPaidAmount);
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
                        receptionSalaryRepo.findByReceptionAndDateRange(
                                reception.getId(),
                                startDate,
                                endDate
                        );

                ReceptionSalary salary = null;

                if (optionalSalary.isPresent()) {
                    salary = optionalSalary.get();
                } else if (isCurrentMonth) {
                    // 🔥 faqat joriy oy uchun yaratamiz
                    salary = new ReceptionSalary();
                    salary.setReceptionist(reception);
                    salary.setSalaryDate(startDate);
                    salary.setSalaryAmount(0);
                    salary = receptionSalaryRepo.save(salary);
                } else {
                    // 🔥 eski oy bo‘lsa skip qilamiz
                    continue;
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
        // TeacherSalary ni topamiz
        TeacherSalary salary = teacherSalaryRepo.findById(salaryId)
                .orElseThrow(() -> new RuntimeException("Salary not found"));

        return salary.getPayments().stream()
                .map(p -> {
                    SalaryPaymentResDto dto = new SalaryPaymentResDto();
                    dto.setId(p.getId());
                    dto.setDate(p.getPaymentDate());
                    dto.setAmount(p.getAmount());
                    dto.setGroupName(salary.getGroup().getName());
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

        // Barcha guruhlar uchun ushbu oyning salary'larini olamiz
        List<TeacherSalary> salaries =
                teacherSalaryRepo.findByTeacherAndDateRange(
                        teacher.getId(),
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
            Group group = salary.getGroup();
            
            // 🔹 Studentlar soni
            int numStudents = (int) group.getGroupStudents().stream()
                    .filter(gs -> gs.getStatus() == GroupStudentStatus.ACTIVE)
                    .count();

            // Guruh bo'yicha qancha tushum bo'ladi
            int mustPaid = 0;
            if (salary.getPercentage() != null && salary.getPercentage() > 0) {
                int perStudentAmount = (coursePaymentAmount * salary.getPercentage()) / 100;
                mustPaid = perStudentAmount * numStudents;
            }
            
            // Jami to'langan summa
            int totalPaid = 0;
            if (salary.getPayments() != null) {
                totalPaid = salary.getPayments().stream().mapToInt(TeacherSalaryPayment::getAmount).sum();
            }

            SalaryByGroupInfoResDto dto = new SalaryByGroupInfoResDto();
            dto.setId(salary.getId()); // E'tibor bering, endi biz TeacherSalary ID sini beramiz! Bu muhim
            dto.setGroupName(group.getName());
            dto.setPercentage(salary.getPercentage() != null ? salary.getPercentage() : 0);
            dto.setMustPaid(mustPaid);
            dto.setAmount(totalPaid);
            dto.setDate(salary.getSalaryDate());

            result.add(dto);
        }

        return result;
    }


    @Transactional
    @Override
    public void addSalaryPayment(UUID salaryId, UUID groupId, Integer amount) {
        // Aslida endi bizga groupId ham kerak emas, chunki TeacherSalary o'zi group ga ulangan.
        // Frontenddan kelayotgan salaryId bu endi TeacherSalary ID si.

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
        // Bu ham endi TeacherSalary ni o'zgartirishi kerak
        TeacherSalary salary = teacherSalaryRepo.findById(salaryId)
                .orElseThrow(() -> new RuntimeException("Teacher salary not found"));
                
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
