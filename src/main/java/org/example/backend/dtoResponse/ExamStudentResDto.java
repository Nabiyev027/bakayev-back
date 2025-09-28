package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ExamStudentResDto {
    private UUID id;
    private String name;
    private List<ExamStudentMarkResDto> marks;

}
