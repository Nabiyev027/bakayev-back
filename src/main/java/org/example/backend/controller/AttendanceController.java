package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.AttendanceGroupDto;
import org.example.backend.dto.AttendanceTodayGroupDto;
import org.example.backend.services.attendance.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/attandance")
@RequiredArgsConstructor
@CrossOrigin
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getTodayAttendanceGroup(@PathVariable UUID groupId) {
        try {
            List<AttendanceTodayGroupDto> todayAttendance = attendanceService.getTodayAttendance(groupId);
            return ResponseEntity.ok(todayAttendance);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{groupId}")
    public ResponseEntity<?> createAttendance(@PathVariable UUID groupId, @RequestBody List<AttendanceGroupDto> attendanceGroupDtos) {
        try {
            attendanceService.markAttendance(groupId,attendanceGroupDtos);
            return ResponseEntity.ok("Attendance entered!");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<?> editAttendance(@PathVariable UUID groupId, @RequestBody List<AttendanceGroupDto> attendanceGroupDtos) {
        try {
            attendanceService.editAttendance(groupId,attendanceGroupDtos);
            return ResponseEntity.ok("Attendance changed!");
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
