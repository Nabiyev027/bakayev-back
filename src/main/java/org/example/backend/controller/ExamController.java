package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.ExamDto;
import org.example.backend.dtoResponse.ExamResDto;
import org.example.backend.dtoResponse.ExamTypeResDto;
import org.example.backend.services.examService.ExamService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN','ROLE_TEACHER','ROLE_STUDENT')")
    @GetMapping("/{groupId}")
    public ResponseEntity<?> getExams(@PathVariable UUID groupId) {
        try {
            List<ExamResDto> exams = examService.getExams(groupId);
            return ResponseEntity.ok(exams);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN','ROLE_TEACHER','ROLE_STUDENT')")
    @GetMapping("/types/{examId}")
    public ResponseEntity<?> getExamTypes(@PathVariable UUID examId) {
        try {
            List<ExamTypeResDto> exams = examService.getExamTypes(examId);
            return ResponseEntity.ok(exams);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN','ROLE_TEACHER')")
    @PostMapping("/add/{groupId}")
    public ResponseEntity<?> addExam(@PathVariable UUID groupId, @RequestBody ExamDto examDto) {
            try {
                examService.addExam(groupId,examDto);
                return ResponseEntity.ok("New exam added!");
            }catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }

    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN','ROLE_TEACHER')")
    @PutMapping("/edit/{examId}")
    public ResponseEntity<?> updateExam(@PathVariable UUID examId, @RequestBody ExamDto examDto) {
        try {
            examService.editExam(examId,examDto);
            return ResponseEntity.ok("Exam updated!");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_ADMIN','ROLE_TEACHER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExam(@PathVariable UUID id) {
        try {
            examService.delExam(id);
            return ResponseEntity.ok("Lesson deleted");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
