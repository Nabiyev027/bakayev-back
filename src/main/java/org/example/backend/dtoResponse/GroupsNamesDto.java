package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class GroupsNamesDto {
    private UUID id;
    private String name;
}
