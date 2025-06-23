package org.example.backend.dtoResponse;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
public class GroupsResDto {
    private UUID id;
    private String name;
    private String degree;
    private RoomResDto roomDto;
    private List<TeacherNameDto> teacherNameDtos;
    private String filialName;
    private Integer StudentsNumber;
    private LocalTime startTime;
    private LocalTime endTime;
}
