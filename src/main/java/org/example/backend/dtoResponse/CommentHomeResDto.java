package org.example.backend.dtoResponse;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CommentHomeResDto {
    private UUID id;
    private String name;
    private String text;
    private LocalDate date;
    private Integer rate;
}
