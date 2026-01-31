package org.example.backend.services.roomService;
import org.example.backend.dto.RoomDto;
import org.example.backend.dto.RoomUpdateDto;
import org.example.backend.dtoResponse.GroupRoomResDto;
import org.example.backend.dtoResponse.RoomResDto;

import java.util.List;
import java.util.UUID;

public interface RoomService {

    void createRoom(UUID filialId , RoomDto roomDto);
    void updateRoom(UUID id, RoomDto roomDto);

    void deleteRoom(UUID id);

    List<RoomResDto> getRooms();

    List<RoomResDto> getRoomsByFilial(UUID filialId);

    List<GroupRoomResDto> getRoomsInfo(String filialId, String dayType);

    void updateRoomGroupInfo(RoomUpdateDto dto);
}
