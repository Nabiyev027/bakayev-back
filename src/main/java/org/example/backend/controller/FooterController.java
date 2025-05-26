package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.FooterSectionDto;
import org.example.backend.entity.FooterSection;
import org.example.backend.services.footerService.FooterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/footerSection")
@RequiredArgsConstructor
@CrossOrigin
public class FooterController {

    private final FooterService footerService;

    @GetMapping
    public ResponseEntity<?> getFooter() {
        try {
            FooterSection info = footerService.getInfo();
            return ResponseEntity.ok(info);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> postFooter(@RequestBody FooterSectionDto footerSectionDto){
        try {
            footerService.postInfo(footerSectionDto);
            return ResponseEntity.ok("Successfully added info");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editAbout(@PathVariable UUID id, @RequestBody FooterSectionDto footerSectionDto){
        try{
            footerService.updateInfo(id,footerSectionDto);
            return ResponseEntity.ok("Successfully updated info");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAbout(@PathVariable UUID id){
        try{
            footerService.deleteInfo(id);
            return ResponseEntity.ok("Successfully deleted");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
