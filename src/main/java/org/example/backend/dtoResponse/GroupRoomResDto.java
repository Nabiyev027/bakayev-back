package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GroupRoomResDto {
    private UUID id;
    private String roomName;
    private Integer roomNumber;
    private List<GroupRoomInfoResDto> groupRoomInfoResDtos;
}
