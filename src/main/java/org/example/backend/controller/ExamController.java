package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.ExamDto;
import org.example.backend.dtoResponse.ExamResDto;
import org.example.backend.dtoResponse.ExamStudentResDto;
import org.example.backend.dtoResponse.ExamTypeResDto;
import org.example.backend.services.examService.ExamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
@CrossOrigin
public class ExamController {

    private final ExamService examService;

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getExams(@PathVariable UUID groupId) {
        try {
            List<ExamResDto> exams = examService.getExams(groupId);
            return ResponseEntity.ok(exams);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/types/{examId}")
    public ResponseEntity<?> getExamTypes(@PathVariable UUID examId) {
        try {
            List<ExamTypeResDto> exams = examService.getExamTypes(examId);
            return ResponseEntity.ok(exams);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/add/{groupId}")
    public ResponseEntity<?> addExam(@PathVariable UUID groupId, @RequestBody ExamDto examDto) {
            try {
                examService.addExam(groupId,examDto);
                return ResponseEntity.ok("New exam added!");
            }catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }

    }

    @PutMapping("/edit/{examId}")
    public ResponseEntity<?> updateExam(@PathVariable UUID examId, @RequestBody ExamDto examDto) {
        try {
            examService.editExam(examId,examDto);
            return ResponseEntity.ok("Exam updated!");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }



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
