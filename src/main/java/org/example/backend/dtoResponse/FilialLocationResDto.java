package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class FilialLocationResDto {
    private UUID id;
    private String name;
    private String description;
    private String location;
    private String imageUrl;
}
