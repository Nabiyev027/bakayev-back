package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class TeacherSectionDataResDto {
    private UUID id;
    private String imgUrl;
    private String firstName;
    private String lastName;
    private String ieltsBall;
    private String certificate;
    private Integer experience;
    private Integer numberOfStudents;
    private String description;

}
