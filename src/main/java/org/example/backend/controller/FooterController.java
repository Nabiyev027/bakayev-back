package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.FooterSectionDto;
import org.example.backend.entity.FooterSection;
import org.example.backend.services.footerService.FooterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/footerSection")
@RequiredArgsConstructor
public class FooterController {

    private final FooterService footerService;

    @GetMapping("/get")
    public ResponseEntity<?> getFooter() {
        try {
            FooterSection info = footerService.getInfo();
            return ResponseEntity.ok(info);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> postAndUpdateFooter(@RequestBody FooterSectionDto footerSectionDto){
        try {
            footerService.postAndUpdateInfo(footerSectionDto);
            return ResponseEntity.ok("Successfully added info");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
