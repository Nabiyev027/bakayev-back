package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class TeacherSectionImgResDto {
    private UUID id;
    private String name;
    private String imageUrl;
    private String ieltsBall;
}
