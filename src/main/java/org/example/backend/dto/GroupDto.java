package org.example.backend.dto;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
public class GroupDto {
    private String name;
    private String degree;
    private UUID roomId;
    private UUID filialId;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<UUID> teacherIds;
    private String dayType;
}
