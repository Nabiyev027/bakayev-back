package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.PaymentDto;
import org.example.backend.dtoResponse.PaymentAmountResDto;
import org.example.backend.dtoResponse.PaymentInfoResDto;
import org.example.backend.dtoResponse.PaymentResDto;
import org.example.backend.entity.PaymentCourseInfo;
import org.example.backend.services.paymentService.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @PostMapping("/addPayment")
    public ResponseEntity<?> addStudentPayment(@RequestBody PaymentDto paymentDto){
        try {
            paymentService.addPayment(paymentDto);
            return ResponseEntity.ok("Payment added successfully!");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @GetMapping("/getPayments")
    public ResponseEntity<?> getPayments(@RequestParam UUID groupId,
                                         @RequestParam(required = false) String dateFrom,
                                         @RequestParam(required = false) String dateTo,
                                         @RequestParam(required = false) String paymentMethod,
                                         @RequestParam(required = false) UUID studentId){
        try {
            if(studentId != null){
                List<PaymentResDto> payment = paymentService.getUserPaymentsWithTransactions(studentId, dateFrom, dateTo, paymentMethod);
                return ResponseEntity.ok(payment);
            }else {
                List<PaymentResDto> payments = paymentService.getPaymentsWithTransaction(groupId, dateFrom, dateTo, paymentMethod);
                return ResponseEntity.ok(payments);
            }

        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping("/courseInfo")
    public ResponseEntity<?> addPaymentInfo(@RequestParam String day, @RequestParam Integer amount){
        try {
            paymentService.addPaymentInfo(day, amount);
            return ResponseEntity.ok("Course payment info added successfully!");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/courseInfo")
    public ResponseEntity<?> getPaymentInfo(){
        try {
            PaymentInfoResDto paymentCourseInfo = paymentService.getPaymentCourseInfo();
            return ResponseEntity.ok(paymentCourseInfo);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_TEACHER','ROLE_ADMIN','ROLE_STUDENT')")
    @GetMapping("/student/{id}")
    public ResponseEntity<?> getStudentPayments(@PathVariable UUID id){
        try {
            List<PaymentResDto> payments = paymentService.getPayments(id);
            return ResponseEntity.ok(payments);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_TEACHER','ROLE_ADMIN','ROLE_STUDENT')")
    @GetMapping("/coursePrice/{id}")
    public ResponseEntity<?> getPaymentInfo(@PathVariable UUID id){
        try {
            Integer paymentInfo = paymentService.getPaymentInfo(id);
            return ResponseEntity.ok(paymentInfo);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_TEACHER','ROLE_ADMIN','ROLE_STUDENT')")
    @GetMapping("/paymentsAmount/{id}")
    public ResponseEntity<?> getPaymentsAmount(@PathVariable String id){
            try {
                List<PaymentAmountResDto> paymentAmounts = paymentService.getPaymentAmounts(id);
                return ResponseEntity.ok(paymentAmounts);
            }catch (Exception e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
    }


    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @DeleteMapping("/delete/{paymentId}")
    public ResponseEntity<?> deletePayment(@PathVariable UUID paymentId) {
        try {
            paymentService.deletePayment(paymentId);
            return ResponseEntity.ok("Deleted successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @DeleteMapping("/transaction/delete/{transactionId}")
    public ResponseEntity<?> deletePaymentTransaction(@PathVariable UUID transactionId) {
        try {
            paymentService.deletePaymentTransaction(transactionId);
            return ResponseEntity.ok("Transaction deleted successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
