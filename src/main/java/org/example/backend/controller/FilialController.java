package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.FilialDto;
import org.example.backend.services.filialService.FilialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/filial")
@RequiredArgsConstructor
@CrossOrigin
public class FilialController {

    private final FilialService filialService;


    @GetMapping("/get")
    public ResponseEntity<?> getFilial() {
        try {
            List<FilialDto> filials = filialService.getFilials();
            return ResponseEntity.ok(filials);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> addFilial(@RequestParam("name") String name, @RequestParam("description") String description,
                                       @RequestParam("location") String location, @RequestParam("image") MultipartFile image) {
        try {
            filialService.createFilial(name,description,location,image);
            return ResponseEntity.ok("Filial added successfully");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateFilial(@RequestParam String id, @RequestParam("name") String name, @RequestParam("description") String description,
                                          @RequestParam("location") String location, @RequestParam("image") MultipartFile image) {
        try {
            filialService.updateFilial(id, name,description,location,image);
            return ResponseEntity.ok("Successfully updated");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Xatolik yuz berdi");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFilial(@PathVariable UUID id) {
        try {
            filialService.deleteFilial(id);
            return ResponseEntity.ok("Successfully deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Xatolik yuz berdi");
        }
    }

}
