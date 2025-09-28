package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class CommentResDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String text;
    private Boolean status;
    private Integer rate;
}
