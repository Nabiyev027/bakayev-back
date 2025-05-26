package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.services.teacherService.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/teacherSection")
@RequiredArgsConstructor
@CrossOrigin
public class TeacherSectionController {
    private final TeacherService teacherService;

    @GetMapping
    public ResponseEntity<?> getTeacherInfo(@RequestParam String lang) {
        try {
            teacherService.getInfo(lang);
            return ResponseEntity.ok("");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> postTeacherInfo(@RequestParam MultipartFile img, @RequestParam String teacherName,
                                             @RequestParam String ieltsBall, @RequestParam String certificate,
                                             @RequestParam String experience, @RequestParam String numberOfStudents,
                                             @RequestParam String description, @RequestParam String lang) {
        teacherService.addInfo(img, teacherName, ieltsBall, certificate, experience, numberOfStudents, description, lang);
        try {
            return ResponseEntity.ok("Successfully added info");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editTeacherInfo(@PathVariable UUID id, @RequestParam MultipartFile img,
                                             @RequestParam String teacherName,
                                             @RequestParam String ieltsBall, @RequestParam String certificate,
                                             @RequestParam String experience, @RequestParam String numberOfStudents,
                                             @RequestParam String description,
                                             @RequestParam String lang) {
        try {
            teacherService.updateInfo(id,img,teacherName,ieltsBall,certificate,experience,numberOfStudents,description,lang);
            return ResponseEntity.ok("Successfully updated info");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeacherInfo(@PathVariable UUID id) {
        try {
            teacherService.deleteTeacher(id);
            return ResponseEntity.ok("Successfully deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
