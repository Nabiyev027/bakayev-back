package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.AttendanceGroupDto;
import org.example.backend.dto.AttendanceTodayGroupDto;
import org.example.backend.dtoResponse.AttendanceResDto;
import org.example.backend.entity.Group;
import org.example.backend.repository.GroupRepo;
import org.example.backend.services.attendance.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@CrossOrigin
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final GroupRepo groupRepo;

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getTodayAttendanceGroup(@PathVariable UUID groupId) {
        try {
            List<AttendanceTodayGroupDto> todayAttendance = attendanceService.getTodayAttendance(groupId);
            return ResponseEntity.ok(todayAttendance);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getAttendance(
            @RequestParam(required = false) UUID group,
            @RequestParam(defaultValue = "daily") String viewType,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().year}") int year,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().monthValue}") int month,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().dayOfMonth}") int day
    ) {
        try {
            // agar frontenddan group UUID kelmasa, birinchi guruhni olib qo‘yamiz
            if (group == null) {
                group = groupRepo.findFirstByOrderByIdAsc()
                        .map(Group::getId)
                        .orElseThrow(() -> new RuntimeException("No group found"));
            }

            LocalDate date = LocalDate.of(year, month, day);
            List<AttendanceResDto> attendanceList = attendanceService.getDailyAttendance(group, date);
            return ResponseEntity.ok(attendanceList);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/day/{groupId}")
    public ResponseEntity<?> addNewAttendance(@PathVariable UUID groupId) {
        try {
            return attendanceService.addNewAttendance(groupId);
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
