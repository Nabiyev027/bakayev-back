package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.StudentMarkDto;
import org.example.backend.dtoResponse.LessonGroupResDto;
import org.example.backend.dtoResponse.LessonStudentByGroupResDto;
import org.example.backend.dtoResponse.LessonStudentResDto;
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
            LessonGroupResDto groupLesson = lessonService.getLessons(groupId);
            return ResponseEntity.ok(groupLesson);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getStudentLessonsByGroupAndUserId(@PathVariable UUID studentId, @RequestParam UUID groupId, @RequestParam String type) {
        try {
            List<LessonStudentByGroupResDto> studentLessons = lessonService.getStudentLessonsByGroupIdAndUserIdAndType(studentId,groupId,type);
            return ResponseEntity.ok(studentLessons);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/studentMarks")
    public ResponseEntity<?> getStudentLessonsByGroupAndUserId(@RequestParam UUID groupId, @RequestParam String type) {
        try {
            List<LessonStudentResDto> studentLessons = lessonService.getStudentLessonsByGroupIdAndType(groupId,type);
            return ResponseEntity.ok(studentLessons);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/changeTime/{groupId}")
    public ResponseEntity<?> changeGroupLessonTime(@PathVariable UUID groupId, @RequestParam String startTime, @RequestParam String endTime ) {
        try {
            lessonService.changeTime(groupId,startTime,endTime);
            return ResponseEntity.ok("Group lesson time was changed");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/saveMarks/{groupId}")
    public ResponseEntity<?> markStudents(
            @PathVariable UUID groupId,
            @RequestBody List<StudentMarkDto> studentMarks
    ) {
        try {
            lessonService.markStudents(groupId, studentMarks);
            return ResponseEntity.ok("Marks saved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroupLesson(@PathVariable UUID id) {
        try {
            lessonService.deleteLesson(id);
            return ResponseEntity.ok("Lesson deleted");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
