package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CourseCardResDto {
    private UUID id;
    private String imgUrl;
    private Integer rating;
    private List<CourseCardTranslationResDto> translations;
}
