package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.FooterSectionDto;
import org.example.backend.entity.FooterSection;
import org.example.backend.services.footerService.FooterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_SUPER_ADMIN','ROLE_ADMIN')")
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
