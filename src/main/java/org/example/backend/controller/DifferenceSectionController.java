package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.DifferenceResDto;
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

    @GetMapping("/get")
    public ResponseEntity<?> getDifferenceSection(){
        try {
            List<DifferenceResDto> difference = differenceService.getDifference();
            return ResponseEntity.ok(difference);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/post")
    public ResponseEntity<?> postDifference(@RequestParam MultipartFile img, @RequestParam String titleUz, @RequestParam String descriptionUz,
                                            @RequestParam String titleRu, @RequestParam String descriptionRu,
                                            @RequestParam String titleEn, @RequestParam String descriptionEn){
        try {
            differenceService.createDifference(img,
                    titleUz,
                    descriptionUz,
                    titleRu,
                    descriptionRu,
                    titleEn,
                    descriptionEn);
            return ResponseEntity.ok("Success");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> editDiference(@PathVariable UUID id,
                                           @RequestParam(value = "img", required = false) MultipartFile img,
                                           @RequestParam String titleUz, @RequestParam String descriptionUz,
                                           @RequestParam String titleRu, @RequestParam String descriptionRu,
                                           @RequestParam String titleEn, @RequestParam String descriptionEn){
        try{
            differenceService.editDif(id,img,titleUz,descriptionUz,titleRu,descriptionRu,titleEn,descriptionEn);
            return ResponseEntity.ok("Success");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteReference(@PathVariable UUID id){
        try{
            differenceService.deleteRef(id);
            return ResponseEntity.ok("Successfully deleted");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



}
