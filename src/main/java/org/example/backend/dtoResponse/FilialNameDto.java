package org.example.backend.dtoResponse;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class FilialNameDto {
    private UUID id;
    private String name;

    public FilialNameDto(UUID id, @NotBlank String name) {
        this.id = id;
        this.name = name;
    }

    public FilialNameDto() {

    }
}
