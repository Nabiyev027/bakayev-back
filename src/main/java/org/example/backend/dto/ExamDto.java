package org.example.backend.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ExamDto {
    private String title;
    private String date;
    private String time;
    private List<UUID> typeIds;
}
