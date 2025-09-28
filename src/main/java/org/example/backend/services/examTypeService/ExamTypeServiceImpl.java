package org.example.backend.services.examTypeService;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.ExamTypeResDto;
import org.example.backend.entity.ExamTypes;
import org.example.backend.repository.ExamTypesRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExamTypeServiceImpl implements ExamTypeService {

    private final ExamTypesRepo examTypesRepo;

    @Override
    public List<ExamTypeResDto> getAllExamTypes() {
        List<ExamTypeResDto> types = new ArrayList<>();
        List<ExamTypes> all = examTypesRepo.findAll();
        all.forEach(examTypes -> {
            ExamTypeResDto dto = new ExamTypeResDto();
            dto.setId(examTypes.getId());
            dto.setName(examTypes.getName());
            types.add(dto);
        });

        return types;
    }

    @Override
    public void addExamType(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Type name cannot be null or empty");
        }

        // Dublikatni tekshirish
        boolean exists = examTypesRepo.existsByNameIgnoreCase(typeName.trim());
        if (exists) {
            throw new RuntimeException("Exam type with name '" + typeName + "' already exists");
        }

        ExamTypes examType = new ExamTypes();
        examType.setName(typeName.trim());
        examTypesRepo.save(examType);
    }

    @Override
    public void deleteExamType(UUID typeId) {
        ExamTypes examType = examTypesRepo.findById(typeId)
                .orElseThrow(() -> new RuntimeException("Exam type not found with id: " + typeId));

        examTypesRepo.delete(examType);
    }

}
