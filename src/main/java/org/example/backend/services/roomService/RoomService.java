package org.example.backend.services.roomService;
import org.example.backend.dto.RoomDto;
import org.example.backend.dtoResponse.RoomResDto;

import java.util.List;
import java.util.UUID;

public interface RoomService {

    void createRoom(UUID filialId , RoomDto roomDto);
    void updateRoom(UUID id, RoomDto roomDto);

    void deleteRoom(UUID id);

    List<RoomResDto> getRooms();
}
