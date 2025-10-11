package org.example.backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AttendanceGroupDto {
    private UUID studentId;
    private String status;
    private String cause;
}
