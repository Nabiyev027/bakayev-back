package org.example.backend.dtoResponse;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceResDto {
    private UUID studentId;
    private String fullName;
    private String status;
    private String reason;
    private Integer percent;
}
