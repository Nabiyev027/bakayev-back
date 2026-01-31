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
    private FilialNameDto filialNameDto;
    private List<TeacherNameDto> teacherNameDtos;
    private Integer studentsNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private String dayType;
}
