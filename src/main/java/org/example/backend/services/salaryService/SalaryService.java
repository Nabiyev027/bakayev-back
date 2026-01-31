package org.example.backend.services.salaryService;

import org.example.backend.dtoResponse.SalaryByGroupInfoResDto;
import org.example.backend.dtoResponse.SalaryPaymentResDto;

import java.util.List;
import java.util.UUID;

public interface SalaryService {

    List<?> getSalaries(String filialId, String role, Integer year, Integer month);

    List<SalaryPaymentResDto> getSalPayments(UUID salaryId);

    List<SalaryByGroupInfoResDto> getSalaryGroupInfo(UUID teacherId, Integer year, Integer month);

    void addSalaryPayment(UUID salaryId,UUID groupId,Integer amount);

    void deleteSalaryPayment(UUID paymentId);

    void updatePercentage(UUID salaryId, Integer percentage);

    void updateReceptionAmount(UUID salaryId, Integer amount);

    void deleteRecSalaryPayment(UUID paymentId);

    List<SalaryPaymentResDto> getSalRecPayments(UUID salaryId);

    void addRecSalaryPayment(UUID salaryId, Integer amount);
}
