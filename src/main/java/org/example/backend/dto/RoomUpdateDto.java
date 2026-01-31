package org.example.backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class RoomUpdateDto {
    private UUID groupId;
    private UUID roomId;
    private String startTime;
    private String endTime;
}
