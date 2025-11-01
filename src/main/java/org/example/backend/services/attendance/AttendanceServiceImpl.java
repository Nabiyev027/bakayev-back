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

    @Transactional
    @Override
    public void markAttendance(UUID groupId, List<AttendanceGroupDto> attendanceGroupDtos) {

        LocalDate today = LocalDate.now();

        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        for (AttendanceGroupDto dto : attendanceGroupDtos) {

            User student = userRepo.findById(dto.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            // Mavjud davomatni topamiz
            Attendance existingAttendance = attendanceRepo.findByGroupAndStudentAndDate(group, student, today);

            if (existingAttendance != null) {
                // Faqat mavjud bo‘lsa — yangilaymiz
                existingAttendance.setStatus(dto.getStatus());
                existingAttendance.setCause(dto.getCause());
                attendanceRepo.save(existingAttendance);
            }
            // Aks holda hech narsa qilmaymiz (yangi yozuv yaratilmaydi)
        }
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
            Group group = groupRepo.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found"));

            List<Attendance> newRecords = group.getStudents().stream().map(student -> {
                Attendance record = new Attendance();
                record.setGroup(group);
                record.setStudent(student);
                record.setDate(date);
                record.setStatus("none");
                record.setCause(null);
                return attendanceRepo.save(record);
            }).toList();

            records = newRecords;
        }

        return mapToDto(records);
    }


    @Transactional
    @Override
    public List<AttendanceDailyResDto> getWeeklyAttendance(UUID groupId, int year, int month, Integer weekNumber) {
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        int targetWeek = weekNumber != null ? weekNumber : firstDayOfMonth.get(WeekFields.ISO.weekOfMonth());
        LocalDate startOfWeek = firstDayOfMonth
                .with(WeekFields.ISO.weekOfMonth(), targetWeek)
                .with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<Attendance> records = attendanceRepo.findByGroup_IdAndDateBetween(groupId, startOfWeek, endOfWeek);

        return mapToDtoWithAverage(records);
    }

    @Transactional
    @Override
    public List<AttendanceDailyResDto> getMonthlyAttendance(UUID groupId, int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        List<Attendance> records = attendanceRepo.findByGroup_IdAndDateBetween(groupId, startOfMonth, endOfMonth);

        return mapToDtoWithAverage(records);
    }

    private List<AttendanceDailyResDto> mapToDtoWithAverage(List<Attendance> records) {
        // Talaba bo‘yicha o‘rtacha percentni hisoblash
        Map<UUID, List<Integer>> studentPercentMap = new HashMap<>();
        for (Attendance record : records) {
            int percent = "present".equals(record.getStatus()) ? 100 : 0;
            studentPercentMap.computeIfAbsent(record.getStudent().getId(), k -> new ArrayList<>()).add(percent);
        }

        Map<UUID, Integer> studentAveragePercent = new HashMap<>();
        studentPercentMap.forEach((studentId, percents) -> {
            int sum = percents.stream().mapToInt(Integer::intValue).sum();
            int avg = percents.isEmpty() ? 0 : sum / percents.size();
            studentAveragePercent.put(studentId, avg);
        });

        // Kunlik DTO larni yaratish
        return records.stream()
                .collect(Collectors.groupingBy(Attendance::getDate))
                .entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<AttendanceResDto> list = entry.getValue().stream()
                            .map(record -> new AttendanceResDto(
                                    record.getStudent().getId(),
                                    record.getStudent().getFirstName() + " " + record.getStudent().getLastName(),
                                    record.getStudent().getPhone(),
                                    record.getStatus(),
                                    record.getCause(),
                                    studentAveragePercent.get(record.getStudent().getId())  // o‘rtacha percent
                            )).toList();
                    return new AttendanceDailyResDto(date, list);
                })
                .sorted(Comparator.comparing(AttendanceDailyResDto::getDate))
                .toList();
    }



    private List<AttendanceDailyResDto> mapToDto(List<Attendance> records) {
        // Sanaga qarab guruhlash
        Map<LocalDate, List<AttendanceResDto>> dailyMap = records.stream()
                .collect(Collectors.groupingBy(Attendance::getDate))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(record -> new AttendanceResDto(
                                        record.getStudent().getId(),
                                        record.getStudent().getFirstName() + " " + record.getStudent().getLastName(),
                                        record.getStudent().getPhone(),
                                        record.getStatus(),
                                        record.getCause(),
                                        "present".equals(record.getStatus()) ? 100 : 0  // kunlik foiz
                                ))
                                .toList()
                ));

        // Talaba bo‘yicha umumiy foiz (haftalik yoki oylik)
        Map<UUID, List<Integer>> studentPercentMap = new HashMap<>();
        for (Attendance record : records) {
            int percent = "present".equals(record.getStatus()) ? 100 : 0;
            studentPercentMap.computeIfAbsent(record.getStudent().getId(), k -> new ArrayList<>()).add(percent);
        }

        Map<UUID, Integer> studentOverallPercent = new HashMap<>();
        studentPercentMap.forEach((studentId, percents) -> {
            int sum = percents.stream().mapToInt(Integer::intValue).sum();
            int avg = percents.isEmpty() ? 0 : sum / percents.size();  // o‘rtacha foiz
            studentOverallPercent.put(studentId, avg);
        });

        // Har bir sanani map qilish va umumiy foizni qo‘shish
        return dailyMap.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<AttendanceResDto> list = entry.getValue().stream()
                            .map(dto -> {
                                dto.setPercent(studentOverallPercent.get(dto.getStudentId())); // haftalik/oylik foiz
                                return dto;
                            }).toList();
                    return new AttendanceDailyResDto(date, list);
                })
                .sorted(Comparator.comparing(AttendanceDailyResDto::getDate))
                .toList();
    }







}
