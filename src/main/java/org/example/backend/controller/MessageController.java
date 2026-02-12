package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.LessonGroupResDto;
import org.example.backend.entity.MessageText;
import org.example.backend.services.messageService.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<?> getMessageTexts() {
        try {
            List<MessageText> messageTexts = messageService.getMessageTexts();
            return ResponseEntity.ok(messageTexts);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> postMessageText(@RequestBody String description) {
        try {
            messageService.addNewMessageDesription(description);
            return ResponseEntity.ok("Added successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessageText(@PathVariable UUID id) {
        try {
            messageService.deleteMessageText(id);
            return ResponseEntity.ok("Lesson deleted");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
