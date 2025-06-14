package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.CourseCard;
import org.example.backend.services.courseCardService.CourseCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/courseCard")
@RequiredArgsConstructor
@CrossOrigin
public class CourseCardController {

    private final CourseCardService courseCardService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getCourseCards() {
        try {
            List<CourseCard> allCards = courseCardService.getAllCards();
            return ResponseEntity.ok(allCards);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/postCourseCard")
    public ResponseEntity<?> addCourseCard(@RequestBody String title, @RequestBody String lang) {
        try {
            courseCardService.addCourseCard(title,lang);
            return ResponseEntity.ok("CourseCard added");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourseCard(@PathVariable UUID id, @RequestBody String title, @RequestBody String lang) {
        try {
            courseCardService.editCourseCard(id,title,lang);
            return ResponseEntity.ok("Course updated");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

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
