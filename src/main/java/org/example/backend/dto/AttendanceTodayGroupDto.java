package org.example.backend.dto;

import lombok.Data;

@Data
public class AttendanceTodayGroupDto {
    private String studentName;
    private String phone;
    private String status;
    private String cause;
}
