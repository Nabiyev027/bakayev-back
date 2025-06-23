package org.example.backend.controller;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.RoomDto;
import org.example.backend.dtoResponse.RoomResDto;
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
            List<RoomResDto> rooms = roomService.getRooms();
            return ResponseEntity.ok(rooms);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{filialId}")
    public ResponseEntity<?> create(@PathVariable UUID filialId ,@RequestBody RoomDto roomDto) {
        try {
            roomService.createRoom(filialId,roomDto);
            return ResponseEntity.ok("Room created successfully");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody RoomDto roomDto) {
        try {
            roomService.updateRoom(id, roomDto);
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
