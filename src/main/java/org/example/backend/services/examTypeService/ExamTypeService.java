package org.example.backend.services.examTypeService;

import org.example.backend.dtoResponse.ExamTypeResDto;

import java.util.List;
import java.util.UUID;

public interface ExamTypeService {
    List<ExamTypeResDto> getAllExamTypes();

    void addExamType(String typeName);

    void deleteExamType(UUID typeId);
}
