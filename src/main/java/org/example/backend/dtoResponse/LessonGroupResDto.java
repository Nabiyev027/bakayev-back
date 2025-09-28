package org.example.backend.dtoResponse;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class LessonGroupResDto {
    private List<LessonStudentResDto> studentsWithResults;
    private LocalTime startTime;
    private LocalTime endTime;
}
