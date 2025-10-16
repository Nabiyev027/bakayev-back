package org.example.backend.services.attendance;

import org.example.backend.dto.AttendanceGroupDto;
import org.example.backend.dto.AttendanceTodayGroupDto;
import org.example.backend.dtoResponse.AttendanceDailyResDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {

    void markAttendance(UUID groupId, List<AttendanceGroupDto> attendanceGroupDtos);

    List<AttendanceTodayGroupDto> getTodayAttendance(UUID groupId);

    List<AttendanceDailyResDto> getDailyAttendance(UUID groupId, LocalDate date);


    List<AttendanceDailyResDto> getMonthlyAttendance(UUID groupId, int y, int m);

    List<AttendanceDailyResDto> getWeeklyAttendance(UUID groupId, int y, int m, Integer week);
}
