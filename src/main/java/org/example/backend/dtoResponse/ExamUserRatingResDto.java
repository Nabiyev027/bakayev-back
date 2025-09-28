package org.example.backend.dtoResponse;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
public class ExamUserRatingResDto {
    private UUID id;
    private String title;
    private LocalDate date;
    private LocalTime startTime;
    private Boolean completed;
    private String groupName;
    private List<ExamStudentMarkResDto> marks;
}
