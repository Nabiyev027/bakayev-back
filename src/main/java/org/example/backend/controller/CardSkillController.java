package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.CardSkill;
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

    @GetMapping("/getAll")
    public ResponseEntity<?> getCardSkill() {
        try {
            List<CardSkill> allCardSkills = cardSkillService.getAllCards();
            return ResponseEntity.ok(allCardSkills);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/postCardSkill")
    public ResponseEntity<?> addCardSkill(@RequestBody String title, @RequestBody String lang) {
        try {
            cardSkillService.addCardSkill(title,lang);
            return ResponseEntity.ok("CardSkill added");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateCardSkill(@PathVariable UUID id, @RequestBody String title, @RequestBody String lang) {
        try {
            cardSkillService.editCardSkill(id,title,lang);
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
