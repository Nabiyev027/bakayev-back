package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class HeaderSectionDto {
    private UUID id;
    private String imgUrl;
    private List<HeaderSectionTranslationResDto> translations;
}
