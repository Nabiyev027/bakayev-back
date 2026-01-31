package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GroupRoomInfoResDto {
    private UUID id;
    private String groupName;
    private String dayType;
    private List<TeacherNameDto> teacherNameDtos;
    private String startTime;
    private String endTime;
}
