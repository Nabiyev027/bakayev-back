package org.example.backend.dtoResponse;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class SalaryTeacherRes {
    private UUID id;
    private UUID teacherId;
    private String fullName;
    private List<String> groupNames;
    private LocalDate date;
}
