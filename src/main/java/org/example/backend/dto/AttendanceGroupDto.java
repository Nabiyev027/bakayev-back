package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
public class AttendanceGroupDto {
    private UUID studentId;
    private Boolean status;
    private String cause;
}
