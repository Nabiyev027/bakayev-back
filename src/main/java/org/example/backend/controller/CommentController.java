package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.CommentDto;
import org.example.backend.dtoResponse.CommentHomeResDto;
import org.example.backend.dtoResponse.CommentResDto;
import org.example.backend.services.CommentService.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@CrossOrigin
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<?> addComment(@RequestBody CommentDto commentDto) {
        try {
            commentService.addComment(commentDto);
            return ResponseEntity.ok("Comment posted");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> allowComment(@PathVariable UUID id) {
        try {
            commentService.allowComment(id);
            return ResponseEntity.ok("Comment confirmed");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getComments() {
        try {
            List<CommentResDto> list = commentService.getComments();
            return ResponseEntity.ok(list);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/home")
    public ResponseEntity<?> getCommentsHome() {
        try {
            List<CommentHomeResDto> list = commentService.getConfirmedComments();
            return ResponseEntity.ok(list);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable UUID id) {
        try {
            commentService.deleteComment(id);
            return ResponseEntity.ok("Comment deleted");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
