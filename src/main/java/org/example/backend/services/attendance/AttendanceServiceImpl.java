package org.example.backend.services.attendance;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.AttendanceGroupDto;
import org.example.backend.dto.AttendanceTodayGroupDto;
import org.example.backend.dtoResponse.AttendanceResDto;
import org.example.backend.entity.Attendance;
import org.example.backend.entity.Group;
import org.example.backend.entity.User;
import org.example.backend.repository.AttendanceRepo;
import org.example.backend.repository.GroupRepo;
import org.example.backend.repository.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final GroupRepo groupRepo;
    private final UserRepo userRepo;
    private final AttendanceRepo attendanceRepo;

    @Override
    public void markAttendance(UUID groupId, List<AttendanceGroupDto> attendanceGroupDtos) {
        attendanceGroupDtos.forEach(user -> {
            Attendance attendance = new Attendance();
            Group group = groupRepo.findById(groupId).get();
            attendance.setGroup(group);
            User student = userRepo.findById(user.getStudentId()).get();
            attendance.setStudent(student);
            attendance.setStatus(user.getStatus());
            attendance.setCause(user.getCause());
            attendance.setDate(LocalDate.now());
            attendanceRepo.save(attendance);
        });

    }

    @Override
    public void editAttendance(UUID groupId, List<AttendanceGroupDto> attendanceGroupDtos) {
        LocalDate today = LocalDate.now();

        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        attendanceGroupDtos.forEach(userDto -> {
            User student = userRepo.findById(userDto.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            // Bugungi sana va studentga tegishli davomatni topamiz
            Optional<Attendance> optionalAttendance = attendanceRepo
                    .findByGroupAndStudentAndDate(group, student, today);

            if (optionalAttendance.isPresent()) {
                Attendance attendance = optionalAttendance.get();
                attendance.setStatus(userDto.getStatus());
                attendance.setCause(userDto.getCause());
                attendanceRepo.save(attendance);
            } else {
                throw new RuntimeException("Attendance not found for student: " + student.getId());
            }
        });
    }

    @Override
    public List<AttendanceTodayGroupDto> getTodayAttendance(UUID groupId) {
        LocalDate today = LocalDate.now();

        List<AttendanceTodayGroupDto> todayAttendance = new ArrayList<>();

        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        List<Attendance> byGroupAndDate = attendanceRepo.findByGroupAndDate(group, today);
        byGroupAndDate.forEach(attendance -> {
            AttendanceTodayGroupDto dto = new AttendanceTodayGroupDto();
            User user = userRepo.findById(attendance.getStudent().getId()).get();
            dto.setStudentName(user.getFirstName() + " " + user.getLastName());
            dto.setPhone(user.getPhone());
            dto.setStatus(attendance.getStatus());
            dto.setCause(attendance.getCause());
            todayAttendance.add(dto);
        });

        return todayAttendance;

    }

    @Override
    public List<AttendanceResDto> getDailyAttendance(UUID group, LocalDate date) {
        List<Attendance> records = attendanceRepo.findByGroup_IdAndDate(group, date);

        return records.stream().map(record -> {
            int percent = Boolean.TRUE.equals(record.getStatus()) ? 100 : 0;
            String fullName = record.getStudent().getFirstName() + " " + record.getStudent().getLastName();

            return new AttendanceResDto(
                    record.getId(),
                    fullName,
                    record.getGroup().getName(),
                    record.getStatus(),
                    record.getCause(),
                    record.getDate(),
                    percent);
        }).collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> addNewAttendance(UUID groupId) {
        Group group = groupRepo.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));

        group.getStudents().forEach(student -> {

            Attendance attendanceByStudent = attendanceRepo.getAttendanceByStudentAndDate(student, LocalDate.now());

            if(attendanceByStudent != null) {
                return;
            }

            Attendance attendance = new Attendance();
            attendance.setGroup(group);
            attendance.setStudent(student);
            attendance.setTeacher(group.getTeachers().getFirst());
            attendance.setStatus(false);
            attendance.setDate(LocalDate.now());
            attendanceRepo.save(attendance);
        });

        List<AttendanceResDto> dailyAttendance = getDailyAttendance(group.getId(), LocalDate.now());

        return ResponseEntity.ok(dailyAttendance);

    }

}
