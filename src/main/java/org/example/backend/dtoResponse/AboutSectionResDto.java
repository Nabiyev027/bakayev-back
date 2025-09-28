package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AboutSectionResDto {
    private UUID id;
    private String imgUrl;
    private String videoUrl;
    private List<AboutSectionTranslationResDto> translations;
}
