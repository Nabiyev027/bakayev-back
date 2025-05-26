package org.example.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotificationDto {
    private List<String> studentsId;
    private String reportStudent;
    private String reportParent;
}
