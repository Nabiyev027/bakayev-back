package org.example.backend.dtoResponse;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
public class GroupsNamesDto {
    private UUID id;
    private String name;

    public GroupsNamesDto(UUID id, @NotBlank String name) {
        this.id = id;
        this.name = name;
    }

    public GroupsNamesDto() {

    }
}
