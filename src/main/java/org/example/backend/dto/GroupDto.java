package org.example.backend.dto;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
public class GroupDto {
    private String name;
    private String degree;
    private LocalTime startTime;
    private LocalTime endTime;
    private UUID roomId;
    private List<UUID> teachersId;
}
