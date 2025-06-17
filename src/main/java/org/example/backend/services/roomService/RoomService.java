package org.example.backend.services.roomService;

import org.example.backend.dtoResponse.RoomDto;

import java.util.List;
import java.util.UUID;

public interface RoomService {

    void createRoom(String name, Integer number, String filialId);
    void updateRoom(UUID id, String name, Integer number);

    void deleteRoom(UUID id);

    List<RoomDto> getRooms();
}
