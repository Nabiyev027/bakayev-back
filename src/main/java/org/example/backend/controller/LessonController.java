package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Lesson;
import org.example.backend.services.lessonService.LessonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/lesson")
@RequiredArgsConstructor
@CrossOrigin
public class LessonController {
    private final LessonService lessonService;

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroupLessons(@PathVariable UUID groupId) {
        try {
            List<Lesson> lessons = lessonService.getLessons(groupId);
            return ResponseEntity.ok(lessons);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{groupId}")
    public ResponseEntity<?> addGroupLesson(@PathVariable UUID groupId, @RequestBody String lessonType) {
        try {
            lessonService.postLesson(groupId,lessonType);
            return ResponseEntity.ok("Lesson added");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PutMapping("/{lessonId}")
    public ResponseEntity<?> updateLesson(@PathVariable UUID lessonId, @RequestBody String lessonType) {
        try {
            lessonService.editLesson(lessonId, lessonType);
            return ResponseEntity.ok("Lesson updated");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroupLesson(@PathVariable UUID id) {
        try {
            lessonService.deletelesson(id);
            return ResponseEntity.ok("Lesson deleted");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



}
