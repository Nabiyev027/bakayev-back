package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class RoomResDto {
    private UUID id;
    private String name;
    private Integer number;
}
