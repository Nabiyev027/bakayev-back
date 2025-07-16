package org.example.backend.services.attendance;

import org.example.backend.dto.AttendanceGroupDto;
import org.example.backend.dto.AttendanceTodayGroupDto;
import org.example.backend.dtoResponse.AttendanceResDto;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {


    void markAttendance(UUID groupId, List<AttendanceGroupDto> attendanceGroupDtos);

    void editAttendance(UUID groupId, List<AttendanceGroupDto> attendanceGroupDtos);

    List<AttendanceTodayGroupDto> getTodayAttendance(UUID groupId);

    List<AttendanceResDto> getDailyAttendance(UUID group, LocalDate date);

    ResponseEntity<?> addNewAttendance(UUID groupId);
}
