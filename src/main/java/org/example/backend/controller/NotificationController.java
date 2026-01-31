package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.NotificationDto;
import org.example.backend.services.notificationService.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
@CrossOrigin
public class NotificationController {


    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody NotificationDto notificationDto) {
        try {
            notificationService.sendMessageToStudentsOrParents(notificationDto);
            return ResponseEntity.ok("Xabar muvaffaqiyatli yuborildi");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
