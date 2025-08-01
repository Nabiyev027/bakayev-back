package org.example.backend.dtoResponse;

import lombok.Data;
import org.example.backend.entity.DifferenceSectionTranslation;

import java.util.List;
import java.util.UUID;

@Data
public class DifferenceResDto {
    private UUID id;
    private String imgUrl;
    private List<DifferenceTranslationResDto> differenceTranslationResDtos;
}
