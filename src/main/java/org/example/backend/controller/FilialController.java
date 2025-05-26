package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Filial;
import org.example.backend.services.filialService.FilialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/filial")
@RequiredArgsConstructor
@CrossOrigin
public class FilialController {

    private final FilialService filialService;

    @GetMapping
    public ResponseEntity<?> getFilial() {
        try {
            List<Filial> filials = filialService.getFilials();
            return ResponseEntity.ok(filials);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> addFilial(@RequestBody Filial filial) {
        try {
            filialService.createFilial(filial);
            return ResponseEntity.ok("Filial added successfully");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFilial(@PathVariable UUID id, @RequestBody Filial filial) {
        try {
            filialService.updateFilial(id, filial);
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
