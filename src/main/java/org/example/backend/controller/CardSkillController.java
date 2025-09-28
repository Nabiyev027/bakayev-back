package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.CardSkillResDto;
import org.example.backend.services.cardSkill.CardSkillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cardSkill")
@RequiredArgsConstructor
@CrossOrigin
public class CardSkillController {

    private final CardSkillService cardSkillService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getCardSkill(@PathVariable UUID id) {
        try {
            List<CardSkillResDto> allCardSkills = cardSkillService.getCardSkills(id);
            return ResponseEntity.ok(allCardSkills);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{cardId}")
    public ResponseEntity<?> addCardSkill(@PathVariable UUID cardId, String titleUz, String titleRu, String titleEn) {
        try {
            cardSkillService.addCardSkill(cardId,titleUz,titleRu,titleEn);
            return ResponseEntity.ok("CardSkill added");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PutMapping("/{skillId}")
    public ResponseEntity<?> updateCardSkill(@PathVariable UUID skillId, @RequestParam String titleUz, @RequestParam String titleRu, @RequestParam String titleEn) {
        try {
            cardSkillService.editCardSkill(skillId,titleUz,titleRu,titleEn);
            return ResponseEntity.ok("Course updated");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCardSkill(@PathVariable UUID id) {
        try {
            cardSkillService.delete(id);
            return ResponseEntity.ok("CardSkill deleted");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
