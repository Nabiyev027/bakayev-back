package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.ExamDto;
import org.example.backend.dto.StudentMarkDto;
import org.example.backend.dtoResponse.ExamGradeResDto;
import org.example.backend.dtoResponse.ExamStudentResDto;
import org.example.backend.dtoResponse.ExamUserRatingResDto;
import org.example.backend.services.examService.ExamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/examGrade")
@RequiredArgsConstructor
@CrossOrigin
public class ExamGradeController {

    private final ExamService examService;

    @GetMapping("/rating/{examId}")
    public ResponseEntity<?> getExamRatings(@PathVariable UUID examId) {
        try {
            List<ExamGradeResDto> examStudents = examService.getExamGradeStudent(examId);
            return ResponseEntity.ok(examStudents);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/saveMarks/{examId}")
    public ResponseEntity<?> markStudents(
            @PathVariable UUID examId,
            @RequestBody List<StudentMarkDto> studentMarks
    ) {
        try {
            examService.markStudents(examId, studentMarks);
            return ResponseEntity.ok("Marks saved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/userRating/{studentId}")
    public ResponseEntity<?> getExams(@PathVariable UUID studentId) {
        try {
            List<ExamUserRatingResDto> examsStudent = examService.getStudentRatings(studentId);
            return ResponseEntity.ok(examsStudent);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



}
