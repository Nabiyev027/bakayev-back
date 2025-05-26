package org.example.backend.services.attendance;

import org.example.backend.dto.AttendanceGroupDto;
import org.example.backend.dto.AttendanceTodayGroupDto;

import java.util.List;
import java.util.UUID;

public interface AttendanceService {


    void markAttendance(UUID groupId, List<AttendanceGroupDto> attendanceGroupDtos);

    void editAttendance(UUID groupId, List<AttendanceGroupDto> attendanceGroupDtos);

    List<AttendanceTodayGroupDto> getTodayAttendance(UUID groupId);
}
