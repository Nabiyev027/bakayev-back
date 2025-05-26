package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.HeaderSectionDto;
import org.example.backend.services.headerService.HeaderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@RestController
@RequestMapping("/headerSection")
@RequiredArgsConstructor
@CrossOrigin
public class HeaderSectionController {
    private final HeaderService headerService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getHeaderSection(@PathVariable UUID id, @RequestParam String lang) {
        try {
            HeaderSectionDto header = headerService.getHeader(id, lang);
            return ResponseEntity.ok(header);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> addHeaderSection(@RequestParam String title, @RequestParam MultipartFile img, @RequestParam String lang) {
        try {
            headerService.postTitle(title,img,lang);
            return ResponseEntity.ok("successfully added header");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editHeaderSection(@PathVariable UUID id, @RequestParam String title, @RequestParam MultipartFile img, @RequestParam String lang) {
        try {
            headerService.editTitle(id,title,img,lang);
            return ResponseEntity.ok("successfully edited header");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
