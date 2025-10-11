package org.example.backend.dtoResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceDailyResDto {
    private LocalDate date;
    private List<AttendanceResDto> attendance;
}
