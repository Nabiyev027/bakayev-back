package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.DifferenceSection;
import org.example.backend.services.differenceService.DifferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/differenceSection")
@RequiredArgsConstructor
@CrossOrigin
public class DifferenceSectionController {

    private final DifferenceService differenceService;

    @GetMapping
    public ResponseEntity<?> getDifferenceSection(){
        try {
            List<DifferenceSection> difference = differenceService.getDifference();
            return ResponseEntity.ok(difference);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> postDifference(@RequestParam MultipartFile img, @RequestParam String title, @RequestParam String description, @RequestParam String lang){
        try {
            differenceService.createDifference(img,title,description,lang);
            return ResponseEntity.ok("Success");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editDiference(@PathVariable UUID id, @RequestParam MultipartFile img, @RequestParam String title, @RequestParam String description, @RequestParam String lang){
        try{
            differenceService.editDif(id,img,title,description,lang);
            return ResponseEntity.ok("Success");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReference(@PathVariable UUID id){
        try{
            differenceService.deleteRef(id);
            return ResponseEntity.ok("Successfully deleted");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



}
