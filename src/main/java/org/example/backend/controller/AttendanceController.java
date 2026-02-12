package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.AttendanceGroupDto;
import org.example.backend.dto.AttendanceTodayGroupDto;
import org.example.backend.dtoResponse.AttendanceDailyResDto;
import org.example.backend.repository.GroupRepo;
import org.example.backend.services.attendance.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_TEACHER','ROLE_ADMIN')")
    @GetMapping("/{groupId}")
    public ResponseEntity<?> getTodayAttendanceGroup(@PathVariable UUID groupId) {
        try {
            List<AttendanceTodayGroupDto> todayAttendance = attendanceService.getTodayAttendance(groupId);
            return ResponseEntity.ok(todayAttendance);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_TEACHER','ROLE_ADMIN')")
    @GetMapping("/get")
    public ResponseEntity<?> getAttendance(
            @RequestParam UUID groupId,
            @RequestParam(defaultValue = "daily") String viewType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) Integer week
    ) {
        try {
            LocalDate now = LocalDate.now();

            int y = year != null ? year : now.getYear();
            int m = month != null ? month : now.getMonthValue();
            int d = day != null ? day : now.getDayOfMonth();

            LocalDate date = LocalDate.of(y, m, d);

            List<AttendanceDailyResDto> attendanceList;

            switch (viewType.toLowerCase()) {
                case "weekly":
                    attendanceList = attendanceService.getWeeklyAttendance(groupId, y, m, week);
                    break;
                case "monthly":
                    attendanceList = attendanceService.getMonthlyAttendance(groupId, y, m);
                    break;
                default:
                    attendanceList = attendanceService.getDailyAttendance(groupId, date);
                    break;
            }

            return ResponseEntity.ok(attendanceList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PreAuthorize("hasAnyRole('ROLE_MAIN_RECEPTION','ROLE_RECEPTION','ROLE_TEACHER','ROLE_ADMIN')")
    @PutMapping("/save/{groupId}")
    public ResponseEntity<?> markAttendance(@PathVariable UUID groupId, @RequestBody List<AttendanceGroupDto> attendanceGroupDtos) {
        try {
            attendanceService.markAttendance(groupId,attendanceGroupDtos);
            return ResponseEntity.ok("Attendance entered!");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



}
