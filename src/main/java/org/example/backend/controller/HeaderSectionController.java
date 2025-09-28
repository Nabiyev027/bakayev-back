package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.HeaderSectionDto;
import org.example.backend.services.headerService.HeaderService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/headerSection")
@RequiredArgsConstructor
@CrossOrigin
public class HeaderSectionController {
    private final HeaderService headerService;

    @Transactional
    @GetMapping
    public ResponseEntity<?> getHeaderSection() {
        try {
            HeaderSectionDto header = headerService.getHeader();
            return ResponseEntity.ok(header);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/post")
    public ResponseEntity<?> addHeaderSection(@RequestParam(required = false) MultipartFile img, @RequestParam String titleUz, @RequestParam String titleRu, @RequestParam String titleEn) {
        try {
            headerService.postOrEdit(img, titleUz, titleRu, titleEn);
            return ResponseEntity.ok("successfully added header");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
