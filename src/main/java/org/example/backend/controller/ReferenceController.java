package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.ReferenceDto;
import org.example.backend.dto.ReferenceWithStatus;
import org.example.backend.entity.Reference;
import org.example.backend.entity.ReferenceStatus;
import org.example.backend.repository.ReferenceRepo;
import org.example.backend.repository.ReferenceStatusRepo;
import org.example.backend.services.referenceService.ReferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reference")
@RequiredArgsConstructor
public class ReferenceController {

    private final ReferenceService referenceService;

    private final ReferenceRepo referenceRepo;
    private final ReferenceStatusRepo referenceStatusRepo;

    @PostMapping("/post")
    public ResponseEntity<?> postRef(@RequestBody ReferenceDto referenceDto) {
        try {
            Reference reference = new Reference();
            reference.setName(referenceDto.getName());
            reference.setPhone(referenceDto.getPhone());
            reference.setTelegramUserName(referenceDto.getTelegramUserName());

            Reference savedReference = referenceRepo.save(reference);
            if (savedReference == null || savedReference.getId() == null) {
                return ResponseEntity.badRequest().body("Reference obyektini saqlab bo‘lmadi!");
            }

            // 2. ReferenceStatus obyektini yaratish va bog‘lash
            ReferenceStatus referenceStatus = new ReferenceStatus();
            referenceStatus.setReference(savedReference);
            referenceStatus.setStatus(false);

            referenceStatusRepo.save(referenceStatus);

            return ResponseEntity.ok("Raxmat, Siz bilan bog'lanishadi");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Xatolik: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @GetMapping("/getAll")
    public ResponseEntity<?> getRef() {
        try {
            List<ReferenceWithStatus> referenceList = referenceService.getReference();
            return ResponseEntity.ok(referenceList);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @PutMapping("/accept/{userId}")
    public ResponseEntity<?> acceptRef(@PathVariable UUID userId, @RequestParam UUID referenceId) {
        try {
            System.out.println(userId);
            System.out.println(referenceId);
            referenceService.acceptReference(userId, referenceId);
            return ResponseEntity.ok("user was called");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN')")
    @DeleteMapping("delete/{referenceId}")
    public ResponseEntity<?> deleteRef(@PathVariable UUID referenceId) {
        try {
            referenceService.deleteReference(referenceId);
            return ResponseEntity.ok("Deleted successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
