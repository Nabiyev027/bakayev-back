package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.NotificationDto;
import org.example.backend.services.notificationService.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {


    private final NotificationService notificationService;

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody NotificationDto notificationDto) {
        try {
            notificationService.sendMessageToStudentsOrParents(notificationDto);
            return ResponseEntity.ok("Message posted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
