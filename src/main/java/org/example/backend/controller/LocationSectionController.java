package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.LocationSection;
import org.example.backend.services.locationService.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/locationSection")
@RequiredArgsConstructor
@CrossOrigin
public class LocationSectionController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<?> getDifferenceSection(){
        try {
            List<LocationSection> locations = locationService.getLocations();
            return ResponseEntity.ok(locations);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> addLocation(@RequestParam MultipartFile img, @RequestParam String address){
        try {
            locationService.addNewLocation(img,address);
            return ResponseEntity.ok("Successfully posted location");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editLocation(@PathVariable UUID id, @RequestParam MultipartFile img, @RequestParam String address){
        try{
            locationService.editLocation(id,img,address);
            return ResponseEntity.ok("Successfully updated location");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLocation(@PathVariable UUID id){
        try{
            locationService.deleteLocation(id);
            return ResponseEntity.ok("Successfully deleted");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
