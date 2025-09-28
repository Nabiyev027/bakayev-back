package org.example.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CommentDto {
    private String firstName;
    private String lastName;
    private String text;
    private Integer rate;
}
