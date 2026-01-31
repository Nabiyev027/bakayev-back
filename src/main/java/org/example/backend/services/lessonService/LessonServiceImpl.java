package org.example.backend.services.lessonService;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.StudentMarkDto;
import org.example.backend.dtoResponse.*;
import org.example.backend.entity.*;
import org.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService{

    private final GroupRepo groupRepo;
    private final LessonRepo lessonRepo;
    private final UserRepo userRepo;
    private final LessonMarkRepo lessonMarkRepo;
    private final LessonTypeRepo lessonTypeRepo;

    @Transactional
    @Override
    public LessonGroupResDto getLessons(UUID groupId) {
        List<Object[]> rows = lessonRepo.getLessonGroupWithStudents(groupId);

        LessonGroupResDto lessonDto = new LessonGroupResDto();
        lessonDto.setStudentsWithResults(new ArrayList<>());

        Map<UUID, LessonStudentResDto> studentMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            // indexlar siz oldin yozgan native query ga mos: 0..7
            Object gIdObj = row[0];
            Object startObj = row[1];
            Object endObj = row[2];
            Object studentIdObj = row[3];
            Object studentNameObj = row[4];
            Object markIdObj = row[5];
            Object typeNameObj = row[6];
            Object markObj = row[7];

            // xavfsiz konvertatsiyalar
            LocalTime startTime = toLocalTime(startObj);
            LocalTime endTime = toLocalTime(endObj);
            UUID studentId = toUUID(studentIdObj);
            UUID markId = toUUID(markIdObj); // null bo'lsa null qaytaradi
            String studentName = studentNameObj == null ? null : studentNameObj.toString();
            String typeName = typeNameObj == null ? null : typeNameObj.toString();
            Integer mark = toInteger(markObj);

            // guruh vaqtlarini bir martada o'rnatamiz (row lar takrorlanadi)
            if (lessonDto.getStartTime() == null) lessonDto.setStartTime(startTime);
            if (lessonDto.getEndTime() == null) lessonDto.setEndTime(endTime);

            // student obyektini map ga qo'shish / topish
            LessonStudentResDto studentDto = studentMap.computeIfAbsent(studentId, id -> {
                LessonStudentResDto dto = new LessonStudentResDto();
                dto.setId(id);
                dto.setName(studentName);
                dto.setLessonMarks(new ArrayList<>());
                return dto;
            });

            if (markId != null) {
                LessonStudentMarksResDto markDto = new LessonStudentMarksResDto();
                markDto.setId(markId);
                markDto.setTypeName(typeName);
                markDto.setMark(mark);
                studentDto.getLessonMarks().add(markDto);
            }
        }

        lessonDto.setStudentsWithResults(new ArrayList<>(studentMap.values()));
        return lessonDto;
    }

    // --- Helper konvertorlar ---
    private LocalTime toLocalTime(Object o) {
        if (o == null) return null;
        if (o instanceof LocalTime) return (LocalTime) o;
        if (o instanceof Time) return ((Time) o).toLocalTime();
        if (o instanceof Timestamp) return ((Timestamp) o).toLocalDateTime().toLocalTime();
        // ba'zan DB String qaytarishi mumkin
        return LocalTime.parse(o.toString());
    }

    private UUID toUUID(Object o) {
        if (o == null) return null;
        if (o instanceof UUID) return (UUID) o;
        return UUID.fromString(o.toString());
    }

    private Integer toInteger(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Number) return ((Number) o).intValue();
        return Integer.valueOf(o.toString());
    }



    @Override
    public void deleteLesson(UUID id) {
        lessonRepo.deleteById(id);
    }

    @Transactional
    @Override
    public void changeTime(UUID groupId, String startTime, String endTime) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));

        group.setStartTime(LocalTime.parse(startTime));
        group.setEndTime(LocalTime.parse(endTime));

        groupRepo.save(group);
    }

    @Transactional
    @Override
    public void markStudents(UUID groupId, List<StudentMarkDto> studentMarks) {
        // 1) Guruhni topish
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        LocalDate today = LocalDate.now();

        // 2) Bugungi dars shu group uchun mavjudmi?
        Lesson lesson = lessonRepo.findByGroupIdAndDate(groupId, today)
                .orElse(null);

        if (lesson == null) {
            // Agar mavjud bo‘lmasa yangi Lesson yaratamiz
            lesson = new Lesson();
            lesson.setGroup(group);
            lesson.setDate(today);
            lesson = lessonRepo.save(lesson);
        }

        // 3) Har bir student uchun mark qo‘shamiz yoki yangilaymiz
        for (StudentMarkDto dto : studentMarks) {
            User student = userRepo.findById(dto.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            LessonTypes type = lessonTypeRepo.findById(dto.getTypeId())
                    .orElseThrow(() -> new RuntimeException("Lesson type not found"));

            // Shu student va shu type bo‘yicha baho mavjudmi?
            LessonMarks existingMark = lessonMarkRepo
                    .findByLessonIdAndStudentIdAndTypeName(lesson.getId(), student.getId(), type.getName())
                    .orElse(null);

            if (existingMark != null) {
                // Mavjud bo‘lsa — update
                existingMark.setMark(dto.getMark());
                lessonMarkRepo.save(existingMark);
            } else {
                // Mavjud bo‘lmasa — yangi baho qo‘shamiz
                LessonMarks mark = new LessonMarks();
                mark.setLesson(lesson);
                mark.setStudent(student);
                mark.setTypeName(type.getName());
                mark.setMark(dto.getMark());
                lessonMarkRepo.save(mark);
            }
        }
    }

    @Transactional
    @Override
    public List<LessonStudentByGroupResDto> getStudentLessonsByGroupIdAndUserIdAndType(UUID studentId, UUID groupId, String type) {
        List<LessonStudentByGroupResDto> studentLessons = new ArrayList<>();

        User student = userRepo.findById(studentId).orElseThrow(() -> new RuntimeException("Student not found"));

        // student – User object
        GroupStudent groupStudent = student.getGroupStudents().stream()
                .filter(gs -> gs.getGroup().getId().equals(groupId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Group not found for student"));

        Group group = groupStudent.getGroup();


        LocalDate now = LocalDate.now();
        LocalDate startDate;

        switch (type.toLowerCase()) {
            case "today":
                startDate = now;
                break;
            case "week":
                startDate = now.minusDays(7);
                break;
            case "month":
                startDate = now.minusMonths(1);
                break;
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }

        LocalDate finalStartDate = startDate;
        group.getLessons().stream()
                .filter(lesson -> {
                    LocalDate d = lesson.getDate();
                    return d != null && !d.isBefore(finalStartDate) && !d.isAfter(now);
                })
                .forEach(lesson -> {
                    LessonStudentByGroupResDto lessonDto = new LessonStudentByGroupResDto();
                    lessonDto.setId(lesson.getId());
                    lessonDto.setDate(lesson.getDate());
                    lessonDto.setWeekDay(lesson.getDate() != null ? lesson.getDate().getDayOfWeek().toString() : null);

                    List<LessonStudentMarksResDto> marks = lesson.getLessonMarks().stream()
                            .filter(mark -> mark.getStudent() != null && mark.getStudent().getId() != null
                                    && mark.getStudent().getId().equals(studentId))
                            .map(mark -> {
                                LessonStudentMarksResDto markDto = new LessonStudentMarksResDto();
                                markDto.setId(mark.getId());
                                markDto.setTypeName(mark.getTypeName());
                                markDto.setMark(mark.getMark());
                                return markDto;
                            })
                            .toList();

                    lessonDto.setMarks(marks);
                    studentLessons.add(lessonDto);
                });

        return studentLessons;
    }

    @Transactional
    @Override
    public List<LessonStudentResDto> getStudentLessonsByGroupIdAndType(UUID groupId, String type) {

        // Natija ro‘yxati
        List<LessonStudentResDto> response = new ArrayList<>();

        // Guruhni olish
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Guruhdagi barcha studentlar (null filter bilan)
        List<User> students = group.getGroupStudents().stream()
                .map(GroupStudent::getStudent)
                .filter(Objects::nonNull)
                .toList();

        LocalDate now = LocalDate.now();
        LocalDate startDate;

        // type bo‘yicha vaqt filtri
        switch (type.toLowerCase()) {
            case "today":
                startDate = now;
                break;
            case "week":
                startDate = now.minusDays(7);
                break;
            case "month":
                startDate = now.minusMonths(1);
                break;
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }

        LocalDate finalStartDate = startDate;

        // Har bir student uchun alohida DTO
        for (User student : students) {

            LessonStudentResDto studentDto = new LessonStudentResDto();
            studentDto.setId(student.getId());
            studentDto.setName(student.getFirstName() + " " + student.getLastName());

            List<LessonStudentMarksResDto> markDtos = new ArrayList<>();

            // Guruhdagi barcha darslar → vaqti bo‘yicha filter qilamiz
            group.getLessons().stream()
                    .filter(lesson -> {
                        LocalDate d = lesson.getDate();
                        return d != null && !d.isBefore(finalStartDate) && !d.isAfter(now);
                    })
                    .forEach(lesson -> {

                        // Studentning ushbu darsdagi baholari
                        lesson.getLessonMarks().stream()
                                .filter(mark -> mark.getStudent() != null) // null-safe filter
                                .filter(mark -> student.getId().equals(mark.getStudent().getId()))
                                .forEach(mark -> {
                                    LessonStudentMarksResDto dto = new LessonStudentMarksResDto();
                                    dto.setId(mark.getId());
                                    dto.setTypeName(mark.getTypeName());
                                    dto.setMark(mark.getMark());
                                    markDtos.add(dto);
                                });

                    });

            studentDto.setLessonMarks(markDtos);
            response.add(studentDto);
        }

        return response;
    }




}
