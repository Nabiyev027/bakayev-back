package org.example.backend.services.roomService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Room;
import org.example.backend.repository.RoomRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepo roomRepo;


    @Override
    public void createRoom(String name, Integer number) {
        Room room = new Room();
        room.setName(name);
        room.setNumber(number);
        roomRepo.save(room);
    }

    @Override
    public void updateRoom(UUID id, String name, Integer number) {
        roomRepo.findById(id).ifPresent(room -> {
            room.setName(name);
            room.setNumber(number);
            roomRepo.save(room);
        });
    }

    @Override
    public void deleteRoom(UUID id) {
        Room room = roomRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));
        roomRepo.delete(room);
    }

    @Override
    public List<Room> getRooms() {
        return roomRepo.findAll();
    }

}
