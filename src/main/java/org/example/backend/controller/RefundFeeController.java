package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.ReferenceDto;
import org.example.backend.dto.RefundDto;
import org.example.backend.dtoResponse.RefundResDto;
import org.example.backend.services.refundService.RefundService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/refund")
@RequiredArgsConstructor
public class RefundFeeController {

    private final RefundService refundService;

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @PostMapping("/post")
    public ResponseEntity<?> postRefund(@RequestBody RefundDto refundDto) {
        try {
            refundService.addRefund(refundDto);
            return ResponseEntity.ok("Refund fee added");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @GetMapping("/get")
    public ResponseEntity<?> getRefund(@RequestParam String filialId, @RequestParam String teacherId, @RequestParam String groupId, @RequestParam String studentId) {
        try {
            List<RefundResDto> refundResDtos = refundService.getRefunds(filialId,teacherId,groupId,studentId);
            return ResponseEntity.ok(refundResDtos);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
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
