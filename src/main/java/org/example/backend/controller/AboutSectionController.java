package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.AboutSectionHomeResDto;
import org.example.backend.dtoResponse.AboutSectionResDto;
import org.example.backend.services.aboutService.AboutService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/home")
    public ResponseEntity<?> getAboutSection(@RequestParam String lang) {
        try {
            AboutSectionHomeResDto about = aboutService.getAboutForHome(lang);
            return ResponseEntity.ok(about);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> aboutPostAndUpdate(
            @RequestParam(required = false) MultipartFile img,
            @RequestParam(required = false) MultipartFile videoImg,
            @RequestParam(required = false) MultipartFile video,
            @RequestParam String description1Uz, @RequestParam String description1Ru,
            @RequestParam String description1En, @RequestParam String description2Uz,
            @RequestParam String description2Ru, @RequestParam String description2En,
            @RequestParam Integer successfulStudents,
            @RequestParam Double averageScore,
            @RequestParam Integer yearsExperience,
            @RequestParam Integer successRate) {


        try {
            aboutService.aboutPostAndUpdate(img, videoImg, video, description1Uz, description1Ru,
                    description1En, description2Uz, description2Ru, description2En,
                    successfulStudents, averageScore, yearsExperience, successRate);
            return ResponseEntity.ok("Successfully added about");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAbout(@PathVariable UUID id) {
        try {
            aboutService.deleteAbout(id);
            return ResponseEntity.ok("Successfully deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
