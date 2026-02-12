package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.SalaryByGroupInfoResDto;
import org.example.backend.dtoResponse.SalaryPaymentResDto;
import org.example.backend.services.salaryService.SalaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/salary")
@RequiredArgsConstructor
public class SalaryController {
    private final SalaryService salaryService;

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @GetMapping("/get")
    public ResponseEntity<?> getEmpSalary(
            @RequestParam(required = false) String filialId,
            @RequestParam String role,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        try {
            List<?> salaries = salaryService.getSalaries(filialId, role, year, month);
            return ResponseEntity.ok(salaries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @GetMapping("/teacher/{salaryId}")
    public ResponseEntity<?> getSalaryPayments(@PathVariable UUID salaryId) {
        try {
            List<SalaryPaymentResDto> payments = salaryService.getSalPayments(salaryId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @GetMapping("/reception/payment/{salaryId}")
    public ResponseEntity<?> getSalaryRecPayments(@PathVariable UUID salaryId) {
        try {
            List<SalaryPaymentResDto> payments = salaryService.getSalRecPayments(salaryId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @GetMapping("/teacher/info")
    public ResponseEntity<?> getSalaryInfo(@RequestParam UUID teacherId,
                                           @RequestParam Integer year,
                                           @RequestParam Integer month) {
        try {
            List<SalaryByGroupInfoResDto> infos = salaryService.getSalaryGroupInfo(teacherId,year,month);
            return ResponseEntity.ok(infos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @PutMapping("/teacher/info/percentage/{salaryId}")
    public ResponseEntity<?> updateSalaryPercentage(@PathVariable UUID salaryId, @RequestParam Integer percentage) {
        try {
            salaryService.updatePercentage(salaryId,percentage);
            return ResponseEntity.ok("Updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @PutMapping("/reception/amount/{salaryId}")
    public ResponseEntity<?> updateReceptionSalaryAmount(@PathVariable UUID salaryId, @RequestParam Integer amount) {
        try {
            salaryService.updateReceptionAmount(salaryId,amount);
            return ResponseEntity.ok("Updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @PostMapping("/teacher/payment/{salaryId}")
    public ResponseEntity<?> postSalaryPayment(@PathVariable UUID salaryId, @RequestParam UUID groupId, @RequestParam Integer amount) {
        try {
            salaryService.addSalaryPayment(salaryId,groupId,amount);
            return ResponseEntity.ok("Added successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @PostMapping("/reception/payment/{salaryId}")
    public ResponseEntity<?> postRecSalaryPayment(@PathVariable UUID salaryId, @RequestParam Integer amount) {
        try {
            salaryService.addRecSalaryPayment(salaryId, amount);
            return ResponseEntity.ok("Added successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @DeleteMapping("/teacher/payment/del/{paymentId}")
    public ResponseEntity<?> deletePayment(@PathVariable UUID paymentId) {
        try {
            salaryService.deleteSalaryPayment(paymentId);
            return ResponseEntity.ok("Deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @DeleteMapping("/reception/payment/del/{paymentId}")
    public ResponseEntity<?> deleteRecPayment(@PathVariable UUID paymentId) {
        try {
            salaryService.deleteRecSalaryPayment(paymentId);
            return ResponseEntity.ok("Deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
