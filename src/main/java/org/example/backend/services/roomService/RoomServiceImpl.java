package org.example.backend.services.roomService;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.RoomDto;
import org.example.backend.dtoResponse.RoomResDto;
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
    public List<RoomResDto> getRooms() {
        List<Room> all = roomRepo.findAll();
        List<RoomResDto> roomDtos = new ArrayList<>();
        for (Room room : all) {
            RoomResDto roomDto = new RoomResDto();
            roomDto.setId(room.getId());
            roomDto.setName(room.getName());
            roomDto.setNumber(room.getNumber());
            roomDtos.add(roomDto);
        }
        return roomDtos;
    }


    @Override
    public void createRoom(UUID filialId, RoomDto roomDto) {
        Room room = new Room();
        room.setName(roomDto.getName());
        room.setNumber(roomDto.getNumber());
        Filial filial = filialRepo.findById(filialId).get();
        room.setFilial(filial);
        roomRepo.save(room);
    }

    @Override
    public void updateRoom(UUID id, RoomDto roomDto) {
        roomRepo.findById(id).ifPresent(room -> {
            room.setName(roomDto.getName());
            room.setNumber(roomDto.getNumber());
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
