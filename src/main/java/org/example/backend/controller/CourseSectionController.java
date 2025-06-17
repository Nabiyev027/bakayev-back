package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
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

    @GetMapping("/getAll")
    public ResponseEntity<?> getCourseWithCard(@RequestParam String lang) {
        try {
            List<CourseSectionWithCardDto> allCourses = courseService.getAllCoursesWithCard(lang);
            System.out.printf("getCourseWithCard: %s\n", allCourses );
            return ResponseEntity.ok(allCourses);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/postCourse")
    public ResponseEntity<?> addCourse(@RequestBody String title, @RequestBody String lang) {
        try {
            courseService.addCourse(title,lang);
            return ResponseEntity.ok("Course added");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable UUID id, @RequestBody String title, @RequestBody String lang) {
        try {
            courseService.editCourse(id,title,lang);
            return ResponseEntity.ok("Course updated");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable UUID id) {
        try {
            courseService.delete(id);
            return ResponseEntity.ok("Lesson deleted");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
