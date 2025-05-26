package org.example.backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TeacherSectionDto {
    private UUID id;
    private String imgUrl;
    private String name;
    private String ieltsBall;
    private String certificate;
    private String experience;
    private String numberOfStudents;
    private String description;
}
