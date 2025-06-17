package org.example.backend.controller;
import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.RoomDto;
import org.example.backend.services.roomService.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
@CrossOrigin
public class RoomController {
    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<?> getAllRooms() {
        try {
            List<RoomDto> rooms = roomService.getRooms();
            return ResponseEntity.ok(rooms);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody String name, @RequestBody Integer number, @RequestBody String filialId) {
        try {
            roomService.createRoom(name, number,filialId);
            return ResponseEntity.ok("Room created successfully");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody String name, @RequestBody Integer number) {
        try {
            roomService.updateRoom(id, name, number);
            return ResponseEntity.ok("Room updated successfully");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        try {
            roomService.deleteRoom(id);
            return ResponseEntity.ok("Room deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Something went wrong");
        }
    }

}
