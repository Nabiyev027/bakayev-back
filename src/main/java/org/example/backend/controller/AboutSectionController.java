package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.AboutSectionResDto;
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
    public ResponseEntity<?> getAbout() {
        try {
            AboutSectionResDto about = aboutService.getAbout();
            return ResponseEntity.ok(about);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> aboutPostAndUpdate(
            @RequestParam(required = false) MultipartFile img,
                                                @RequestParam(required = false) MultipartFile video,
                                                @RequestParam String description1Uz, @RequestParam String description1Ru,
                                                @RequestParam String description1En, @RequestParam String description2Uz,
                                                @RequestParam String description2Ru, @RequestParam String description2En){
        try {
            aboutService.aboutPostAndUpdate(img,video,description1Uz,description1Ru,description1En,description2Uz,description2Ru,description2En);
            return ResponseEntity.ok("Successfully added about");
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
