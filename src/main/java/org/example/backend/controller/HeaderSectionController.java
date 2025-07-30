package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.HeaderSectionDto;
import org.example.backend.services.headerService.HeaderService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@RestController
@RequestMapping("/headerSection")
@RequiredArgsConstructor
@CrossOrigin
public class HeaderSectionController {
    private final HeaderService headerService;

    @Transactional
    @GetMapping
    public ResponseEntity<?> getHeaderSection(@RequestParam String lang) {
        try {
            HeaderSectionDto header = headerService.getHeader(lang);
            return ResponseEntity.ok(header);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/post")
    public ResponseEntity<?> addHeaderSection(@RequestParam String title, @RequestParam MultipartFile img, @RequestParam String lang) {
        try {
            headerService.postOrEdit(title,img,lang);
            return ResponseEntity.ok("successfully added header");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
