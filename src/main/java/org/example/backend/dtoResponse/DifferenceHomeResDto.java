package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class DifferenceHomeResDto {
    private UUID id;
    private String imgUrl;
    private String title;
    private String description;
}
