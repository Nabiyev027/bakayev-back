package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.LessonTypeResDto;
import org.example.backend.services.lessonTypeService.LessonTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lessonTypes")
@RequiredArgsConstructor
public class LessonTypeController {

    private final LessonTypeService lessonTypeService;

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN','ROLE_TEACHER')")
    @GetMapping
    public ResponseEntity<?> getLessonTypes() {
        try {
            List<LessonTypeResDto> types = lessonTypeService.getLessonTypes();
            return ResponseEntity.ok(types);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN','ROLE_TEACHER')")
    @PostMapping
    public ResponseEntity<?> addLessonType(@RequestParam String typeName) {
        try {
            lessonTypeService.postLessonType(typeName);
            return ResponseEntity.ok("Lesson Type added");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN','ROLE_TEACHER')")
    @DeleteMapping("/{typeId}")
    public ResponseEntity<?> deleteLessonType(@PathVariable UUID typeId) {
        try {
            lessonTypeService.deleteType(typeId);
            return ResponseEntity.ok("LessonType deleted");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
