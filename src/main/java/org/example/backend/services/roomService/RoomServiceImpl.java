package org.example.backend.services.roomService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.RoomDto;
import org.example.backend.entity.Filial;
import org.example.backend.entity.Room;
import org.example.backend.repository.FilialRepo;
import org.example.backend.repository.RoomRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepo roomRepo;
    private final FilialRepo filialRepo;

    @Override
    public List<RoomDto> getRooms() {
        List<Room> all = roomRepo.findAll();
        List<RoomDto> roomDtos = new ArrayList<>();
        for (Room room : all) {
            RoomDto roomDto = new RoomDto();
            roomDto.setId(room.getId());
            roomDto.setName(room.getName());
            roomDto.setNumber(room.getNumber());
            roomDtos.add(roomDto);
        }
        return roomDtos;
    }


    @Override
    public void createRoom(String name, Integer number, String filialId) {
        Room room = new Room();
        room.setName(name);
        room.setNumber(number);
        Filial filial = filialRepo.findById(UUID.fromString(filialId)).get();
        room.setFilial(filial);
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
                .orElseThrow(() -> new RuntimeException("Room not found"));
        roomRepo.delete(room);
    }

}
