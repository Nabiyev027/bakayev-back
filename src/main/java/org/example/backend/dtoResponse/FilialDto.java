package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class FilialDto {
    private UUID id;
    private String name;
    private String location;
    private String description;
    private String imageUrl;
}
