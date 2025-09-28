package org.example.backend.services.lessonTypeService;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtoResponse.LessonTypeResDto;
import org.example.backend.entity.LessonTypes;
import org.example.backend.repository.LessonTypeRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonTypeServiceImpl implements LessonTypeService {

    private final LessonTypeRepo lessonTypeRepo;


    @Override
    public List<LessonTypeResDto> getLessonTypes() {
        List<LessonTypeResDto> types = new ArrayList<>();
        List<LessonTypes> all = lessonTypeRepo.findAll();
        all.forEach(lessonType -> {
            LessonTypeResDto lessonTypeResDto = new LessonTypeResDto();
            lessonTypeResDto.setId(lessonType.getId());
            lessonTypeResDto.setName(lessonType.getName());
            lessonTypeResDto.setStatus(lessonType.getStatus());
            types.add(lessonTypeResDto);
        });

        return types;
    }

    @Transactional
    @Override
    public void postLessonType(String typeName) {
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Type name bo‘sh bo‘lishi mumkin emas");
        }

        String normalizedName = typeName.trim();

        // katta-kichik harfni farqlamasdan tekshirish
        if (lessonTypeRepo.existsByNameIgnoreCase(normalizedName)) {
            throw new RuntimeException("Bunday lesson type allaqachon mavjud: " + typeName);
        }

        LessonTypes lessonType = new LessonTypes();
        lessonType.setName(normalizedName);
        lessonType.setStatus(false);

        lessonTypeRepo.save(lessonType);
    }

    @Transactional
    @Override
    public void deleteType(UUID lessonTypeId) {
        LessonTypes lessonType = lessonTypeRepo.findById(lessonTypeId)
                .orElseThrow(() -> new RuntimeException("Lesson type topilmadi: " + lessonTypeId));

        lessonTypeRepo.delete(lessonType);
    }


}
