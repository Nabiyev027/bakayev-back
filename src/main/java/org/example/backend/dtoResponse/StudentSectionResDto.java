package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class StudentSectionResDto {
    private UUID id;
    private String imgUrl;
    private String name;
    private Double listening;
    private Double reading;
    private Double writing;
    private Double speaking;
    private Double overall;
}
