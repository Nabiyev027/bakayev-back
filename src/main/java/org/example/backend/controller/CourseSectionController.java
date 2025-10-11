package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.CourseSectionResDto;
import org.example.backend.dtoResponse.CourseSectionWithCardDto;
import org.example.backend.services.courseService.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/courseSection")
@RequiredArgsConstructor
@CrossOrigin
public class CourseSectionController {
    private final CourseService courseService;

    @GetMapping("/getHome")
    public ResponseEntity<?> getCourseWithCard(@RequestParam String lang) {
        try {
            List<CourseSectionWithCardDto> allCourses = courseService.getAllCoursesWithCard(lang);
            return ResponseEntity.ok(allCourses);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getCourse() {
        try {
            List<CourseSectionResDto> courseSectionResDtos = courseService.getCourses();
            return ResponseEntity.ok(courseSectionResDtos);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> addCourse(@RequestParam String titleUz, @RequestParam String titleRu, @RequestParam String titleEn) {
        try {
            courseService.addCourse(titleUz,titleRu,titleEn);
            return ResponseEntity.ok("New Course added");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable UUID id, @RequestParam String titleUz, @RequestParam String titleRu, @RequestParam String titleEn) {
        try {
            courseService.editCourse(id,titleUz, titleRu, titleEn);
            return ResponseEntity.ok("Course updated");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable UUID id) {
        try {
            courseService.delete(id);
            return ResponseEntity.ok("Course deleted");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
