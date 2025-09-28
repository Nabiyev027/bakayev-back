package org.example.backend.dtoResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
public class GroupsNamesDto {
    private UUID id;
    private String name;
}
