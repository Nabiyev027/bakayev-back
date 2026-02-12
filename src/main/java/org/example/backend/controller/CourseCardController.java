package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.CourseCardResDto;
import org.example.backend.services.courseCardService.CourseCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/courseCard")
@RequiredArgsConstructor
public class CourseCardController {

    private final CourseCardService courseCardService;

    @GetMapping("/{CourseId}")
    public ResponseEntity<?> getCourseCards(@PathVariable UUID CourseId) {
        try {
            List<CourseCardResDto> allCards = courseCardService.getAllCards(CourseId);
            return ResponseEntity.ok(allCards);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @PostMapping("/{perId}")
    public ResponseEntity<?> addCourseCard(@PathVariable UUID perId, @RequestParam MultipartFile img,
                                           @RequestParam String titleUz, @RequestParam String titleRu,
                                           @RequestParam String titleEn, @RequestParam Integer rating) {
        try {
            courseCardService.addCourseCard(perId, img, titleUz, titleRu, titleEn, rating);
            return ResponseEntity.ok("CourseCard added");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourseCard(@PathVariable UUID id,
                                              @RequestParam(required = false) MultipartFile img,
                                              @RequestParam String titleUz, @RequestParam String titleRu,
                                              @RequestParam String titleEn, @RequestParam Integer rating) {
        try {
            courseCardService.editCourseCard(id,img,titleUz,titleRu,titleEn,rating);
            return ResponseEntity.ok("Course updated");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_RECEPTION','ROLE_MAIN_RECEPTION','ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourseCard(@PathVariable UUID id) {
        try {
            courseCardService.delete(id);
            return ResponseEntity.ok("CourseCard deleted");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
