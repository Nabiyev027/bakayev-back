package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.ReferenceDto;
import org.example.backend.dto.RefundDto;
import org.example.backend.dtoResponse.RefundResDto;
import org.example.backend.services.refundService.RefundService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/refund")
@RequiredArgsConstructor
@CrossOrigin
public class RefundFeeController {

    private final RefundService refundService;

    @PostMapping("/post")
    public ResponseEntity<?> postRefund(@RequestBody RefundDto refundDto) {
        try {
            refundService.addRefund(refundDto);
            return ResponseEntity.ok("Thank you, you will be contacted.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getRefund(@RequestParam String filialId, @RequestParam String teacherId, @RequestParam String groupId, @RequestParam String studentId) {
        try {
            List<RefundResDto> refundResDtos = refundService.getRefunds(filialId,teacherId,groupId,studentId);
            return ResponseEntity.ok(refundResDtos);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{refundId}")
    public ResponseEntity<?> deleteRefund(@PathVariable UUID refundId) {
        try {
            refundService.deleteRefund(refundId);
            return ResponseEntity.ok("Refund fee is deleted.");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

}
