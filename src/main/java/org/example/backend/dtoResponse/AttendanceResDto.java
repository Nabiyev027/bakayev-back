package org.example.backend.dtoResponse;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceResDto {
    private UUID id;
    private String studentFullName;
    private String groupName;
    private Boolean status; // true = Kelgan, false = Kelmagan
    private String cause;
    private LocalDate date;
    private int attendancePercent; // 100% yoki 0%
}
