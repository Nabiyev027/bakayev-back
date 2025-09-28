package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.PaymentDto;
import org.example.backend.dtoResponse.PaymentInfoResDto;
import org.example.backend.entity.PaymentCourseInfo;
import org.example.backend.services.paymentService.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@CrossOrigin
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> addStudentPayment(@RequestBody PaymentDto paymentDto){
        try {
            paymentService.addPayment(paymentDto);
            return ResponseEntity.ok("Payment added successfully!");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

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



}
