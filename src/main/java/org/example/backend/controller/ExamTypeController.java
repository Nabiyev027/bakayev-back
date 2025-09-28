package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.ExamTypeResDto;
import org.example.backend.services.examTypeService.ExamTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/examTypes")
@RequiredArgsConstructor
@CrossOrigin
public class ExamTypeController {


    private final ExamTypeService examTypeService;

    @GetMapping
    public ResponseEntity<?> getExamTypes() {
        try {
            List<ExamTypeResDto> types = examTypeService.getAllExamTypes();
            return ResponseEntity.ok(types);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> postExamType(@RequestParam String typeName) {
        try {
            examTypeService.addExamType(typeName);
            return ResponseEntity.ok("Exam Type added");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{typeId}")
    public ResponseEntity<?> deleteLessonType(@PathVariable UUID typeId) {
        try {
            examTypeService.deleteExamType(typeId);
            return ResponseEntity.ok("Exam Type deleted");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
