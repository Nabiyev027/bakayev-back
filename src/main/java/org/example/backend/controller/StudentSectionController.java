package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.StudentSectionResDto;
import org.example.backend.entity.StudentSection;
import org.example.backend.services.studentService.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/studentSection")
@RequiredArgsConstructor
@CrossOrigin
public class StudentSectionController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<?> getStudentInfo() {
        try {
            List<StudentSectionResDto> studentInfo = studentService.getStudentInfo();
            return ResponseEntity.ok(studentInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> postStudentInfo(@RequestParam MultipartFile img, @RequestParam String name,
                                             @RequestParam Double listening, @RequestParam Double writing,
                                             @RequestParam Double reading,
                                             @RequestParam Double speaking,
                                             @RequestParam Double overall) {
        try {
            studentService.addStudent(img, name, listening, reading, writing, speaking, overall);
            return ResponseEntity.ok("Successfully added Student");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editStudentInfo(@PathVariable UUID id, @RequestParam(required = false) MultipartFile img,
                                             @RequestParam String name, @RequestParam Double listening,
                                             @RequestParam Double writing, @RequestParam Double reading,
                                             @RequestParam Double speaking, @RequestParam Double overall) {
        try {
            studentService.updateStudent(id, img,name,listening,writing,reading,speaking,overall);
            return ResponseEntity.ok("Successfully updated Student");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudentInfo(@PathVariable UUID id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.ok("Successfully deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
