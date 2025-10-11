package org.example.backend.dtoResponse;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class LessonStudentByGroupResDto {
    private UUID id;
    private LocalDate date;
    private String weekDay;
    private List<LessonStudentMarksResDto> marks;

}
