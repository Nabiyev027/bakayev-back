package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.TeacherSectionResDto;
import org.example.backend.services.teacherService.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/teacherSection")
@RequiredArgsConstructor
@CrossOrigin
public class TeacherSectionController {
    private final TeacherService teacherService;

    @GetMapping
    public ResponseEntity<?> getTeacherInfo() {
        try {
            List<TeacherSectionResDto> teacherSections = teacherService.getTeacherSections();
            return ResponseEntity.ok(teacherSections);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> postTeacherInfo(@RequestParam MultipartFile img,
                                             @RequestParam String firstName, @RequestParam String lastName,
                                             @RequestParam String ieltsBall, @RequestParam String certificate,
                                             @RequestParam Integer experience, @RequestParam Integer numberOfStudents,
                                             @RequestParam String descriptionUz, @RequestParam String descriptionRu, @RequestParam String descriptionEn) {
        try {
            teacherService.addInfo(img, firstName, lastName, ieltsBall, certificate, experience, numberOfStudents, descriptionUz, descriptionRu, descriptionEn);
            return ResponseEntity.ok("Successfully added info");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editTeacherInfo(@PathVariable UUID id, @RequestParam(required = false) MultipartFile img,
                                             @RequestParam String firstName, @RequestParam String lastName,
                                             @RequestParam String ieltsBall, @RequestParam String certificate,
                                             @RequestParam Integer experience, @RequestParam Integer numberOfStudents,
                                             @RequestParam String descriptionUz, @RequestParam String descriptionRu, @RequestParam String descriptionEn) {
        try {
            teacherService.updateInfo(id,img,firstName,lastName,ieltsBall,certificate,experience,numberOfStudents,descriptionUz,descriptionRu,descriptionEn);
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
