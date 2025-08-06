package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.AboutSectionDto;
import org.example.backend.services.aboutService.AboutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.UUID;

@RestController
@RequestMapping("/aboutSection")
@RequiredArgsConstructor
@CrossOrigin
public class AboutSectionController {

    private final AboutService aboutService;

    @GetMapping
    public ResponseEntity<?> getAbout(@RequestParam String lang) {
        try {
            AboutSectionDto about = aboutService.getAbout(lang);
            return ResponseEntity.ok(about);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> aboutPostAndUpdate(
            @RequestParam(required = false) MultipartFile img,
                                                @RequestParam(required = false) MultipartFile video,
                                                @RequestParam String description1,
                                                @RequestParam String description2,
                                                @RequestParam String lang){
        try {
            aboutService.aboutPostAndUpdate(img,video,description1,description2,lang);
            return ResponseEntity.ok("Successfully added about");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editAbout(@PathVariable UUID id, @RequestParam MultipartFile img, @RequestParam String video, @RequestParam String description1, @RequestParam String description2, @RequestParam String lang){
        try{
            aboutService.editAbout(id,img,video,description1,description2,lang);
            return ResponseEntity.ok("Successfully updated about");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAbout(@PathVariable UUID id){
        try{
            aboutService.deleteAbout(id);
            return ResponseEntity.ok("Successfully deleted");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
