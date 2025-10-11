package org.example.backend.services.attendance;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.AttendanceGroupDto;
import org.example.backend.dto.AttendanceTodayGroupDto;
import org.example.backend.dtoResponse.AttendanceDailyResDto;
import org.example.backend.dtoResponse.AttendanceResDto;
import org.example.backend.entity.Attendance;
import org.example.backend.entity.Group;
import org.example.backend.entity.User;
import org.example.backend.repository.AttendanceRepo;
import org.example.backend.repository.GroupRepo;
import org.example.backend.repository.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
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

    @Transactional
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

    @Transactional
    @Override
    public List<AttendanceDailyResDto> getDailyAttendance(UUID groupId, LocalDate date) {
        List<Attendance> records = attendanceRepo.findByGroup_IdAndDate(groupId, date);

        // Agar hozircha hech narsa yozilmagan bo‘lsa, yangi yozuvlar yaratamiz
        if (records.isEmpty()) {
            Group group = groupRepo.findById(groupId).orElseThrow();

            for (User student : group.getStudents()) {
                Attendance newRecord = new Attendance();
                newRecord.setGroup(group);
                newRecord.setStudent(student);
                newRecord.setDate(date);
                newRecord.setStatus(null);
                newRecord.setCause(null);
                attendanceRepo.save(newRecord);
            }

            records = attendanceRepo.findByGroup_IdAndDate(groupId, date);
        }

        return mapToDto(records);
    }



    @Override
    public List<AttendanceDailyResDto> getWeeklyAttendance(UUID groupId, int year, int month, Integer weekNumber) {
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        int targetWeek = weekNumber != null ? weekNumber : firstDayOfMonth.get(WeekFields.ISO.weekOfMonth());
        LocalDate startOfWeek = firstDayOfMonth
                .with(WeekFields.ISO.weekOfMonth(), targetWeek)
                .with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<Attendance> records = attendanceRepo.findByGroup_IdAndDateBetween(groupId, startOfWeek, endOfWeek);
        return mapToDto(records);
    }

    @Override
    public List<AttendanceDailyResDto> getMonthlyAttendance(UUID groupId, int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        List<Attendance> records = attendanceRepo.findByGroup_IdAndDateBetween(groupId, startOfMonth, endOfMonth);
        return mapToDto(records);
    }



    private List<AttendanceDailyResDto> mapToDto(List<Attendance> records) {
        return records.stream()
                .collect(Collectors.groupingBy(Attendance::getDate)) // sanaga qarab guruhlash
                .entrySet()
                .stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<AttendanceResDto> attendanceList = entry.getValue().stream()
                            .map(record -> {
                                int percent = "present".equals(record.getStatus()) ? 100 : 0;
                                String fullName = record.getStudent().getFirstName() + " " + record.getStudent().getLastName();
                                return new AttendanceResDto(
                                        record.getStudent().getId(),
                                        fullName,
                                        record.getStatus(),
                                        record.getCause(),
                                        percent
                                );
                            })
                            .toList();

                    return new AttendanceDailyResDto(date, attendanceList);
                })
                .sorted(Comparator.comparing(AttendanceDailyResDto::getDate)) // sanalar tartibda bo‘lsin
                .toList();
    }


}
