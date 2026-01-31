package org.example.backend.services.examService;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.ExamDto;
import org.example.backend.dto.StudentMarkDto;
import org.example.backend.dtoResponse.*;
import org.example.backend.entity.*;
import org.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamRepo examRepo;
    private final GroupRepo groupRepo;
    private final ExamTypesRepo examTypesRepo;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final ExamGradesRepo examGradesRepo;

    @Transactional
    @Override
    public List<ExamResDto> getExams(UUID groupId) {
        Group group = groupRepo.findById(groupId).get();
        List<Exam> allByGroup = examRepo.findAllByGroup(group);
        List<ExamResDto> examDtos = new ArrayList<>();

        allByGroup.forEach(exam -> {
            ExamResDto examDto = new ExamResDto();
            examDto.setId(exam.getId());
            examDto.setTitle(exam.getTitle());
            examDto.setDate(exam.getDate());
            examDto.setStartTime(exam.getStartTime());
            examDto.setCompleted(exam.getCompleted());
            List<ExamTypeResDto> examTypes = new ArrayList<>();
            exam.getExamTypes().forEach(examType -> {
                ExamTypeResDto type = new ExamTypeResDto();
                type.setId(examType.getId());
                type.setName(examType.getName());
                examTypes.add(type);
            });

            examDto.setExamTypes(examTypes);
            examDtos.add(examDto);

        });

        return examDtos;
    }

    @Transactional
    @Override
    public void editExam(UUID examId, ExamDto examDto) {
        Exam exam = examRepo.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));

        exam.setTitle(examDto.getTitle());
        exam.setDate(LocalDate.parse(examDto.getDate()));
        exam.setStartTime(LocalTime.parse(examDto.getTime()));

        // typeIds bo‘yicha examTypes ni yangilash
        List<ExamTypes> examTypes = examDto.getTypeIds().stream()
                .map(typeId -> examTypesRepo.findById(typeId)
                        .orElseThrow(() -> new RuntimeException("ExamType not found with id: " + typeId)))
                .collect(Collectors.toList());

        exam.setExamTypes(examTypes); // eski listni o‘chirib, yangisini qo‘yadi

        examRepo.save(exam); // transaction oxirida update bo‘ladi
    }

    @Transactional
    @Override
    public void delExam(UUID id) {
        Exam exam = examRepo.findById(id).orElseThrow(() -> new RuntimeException("Exam not found with id: " + id));
        examRepo.delete(exam);
    }

    @Transactional
    @Override
    public List<ExamTypeResDto> getExamTypes(UUID examId) {
        List<ExamTypeResDto> examDtos = new ArrayList<>();
        Exam exam = examRepo.findById(examId).orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));
        exam.getExamTypes().forEach(examType -> {
            ExamTypeResDto examTypeResDto = new ExamTypeResDto();
            examTypeResDto.setId(examType.getId());
            examTypeResDto.setName(examType.getName());
            examDtos.add(examTypeResDto);
        });

        return examDtos;

    }

    @Transactional
    @Override
    public List<ExamGradeResDto> getExamGradeStudent(UUID examId) {
        List<ExamGradeResDto> examGradeResDtos = new ArrayList<>();

        // Examni olish
        Exam exam = examRepo.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));
        Group group = exam.getGroup();

        // Student role ni olish
        Role roleStudent = roleRepo.findByName("ROLE_STUDENT")
                .orElseThrow(() -> new RuntimeException("ROLE_STUDENT not found"));

        // Shu guruhdagi barcha studentlarni olish
        List<StudentProjection> usersByGroupAndRole = userRepo.findUsersByGroupAndRole(group, roleStudent);

        // Har bir student uchun DTO yasash
        usersByGroupAndRole.forEach(user -> {
            ExamGradeResDto examGradeResDto = new ExamGradeResDto();
            examGradeResDto.setId(user.getId());
            examGradeResDto.setName(user.getFirstName() + " " + user.getLastName());

            // Shu studentning shu examdagi barcha baholari
            List<ExamGrades> studentGrades = exam.getExamGrades().stream()
                    .filter(grade -> grade.getStudent().getId().equals(user.getId()))
                    .toList();

            // ExamGrades -> ExamStudentMarkResDto
            List<ExamStudentMarkResDto> marks = studentGrades.stream().map(grade -> {
                ExamStudentMarkResDto dto = new ExamStudentMarkResDto();
                dto.setId(grade.getId());
                dto.setTypeName(grade.getTypeName());
                dto.setMark(grade.getScore());
                return dto;
            }).toList();

            examGradeResDto.setMarks(marks);

            examGradeResDtos.add(examGradeResDto);
        });

        return examGradeResDtos;
    }





    @Transactional
    @Override
    public void markStudents(UUID examId, List<StudentMarkDto> studentMarks) {
        Exam exam = examRepo.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));

        for (StudentMarkDto dto : studentMarks) {
            User student = userRepo.findById(dto.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            ExamTypes type = examTypesRepo.findById(dto.getTypeId())
                    .orElseThrow(() -> new RuntimeException("Exam type not found"));

            // 🔎 Avval shu student + exam + type bo‘yicha baho borligini tekshiramiz
            Optional<ExamGrades> existingGradeOpt =
                    examGradesRepo.findByExamAndStudentAndTypeName(exam, student, type.getName());

            ExamGrades examGrade;
            if (existingGradeOpt.isPresent()) {
                // Agar yozuv bo‘lsa -> edit qilamiz
                examGrade = existingGradeOpt.get();
            } else {
                // Agar yozuv bo‘lmasa -> yangi qo‘shamiz
                examGrade = new ExamGrades();
                examGrade.setExam(exam);
                examGrade.setStudent(student);
                examGrade.setTypeName(type.getName());
            }

            examGrade.setScore(dto.getMark()); // qiymatni yangilash yoki qo‘yish
            examGradesRepo.save(examGrade);
        }

        exam.setCompleted(true);
        examRepo.save(exam);
    }

    @Override
    @Transactional
    public List<ExamUserRatingResDto> getStudentRatings(UUID studentId) {
        User user = userRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Student bo'lgan guruhlar
        List<Group> studentGroups = user.getGroupStudents().stream()
                .map(GroupStudent::getGroup)
                .toList();

        if (studentGroups.isEmpty()) {
            return Collections.emptyList();
        }

        // Shu guruhlardagi examlarni olish
        List<Exam> exams = examRepo.findByGroupIn(studentGroups);

        List<ExamUserRatingResDto> studentRatings = new ArrayList<>();

        for (Exam exam : exams) {
            ExamUserRatingResDto examDto = new ExamUserRatingResDto();
            examDto.setId(exam.getId());
            examDto.setTitle(exam.getTitle());
            examDto.setDate(exam.getDate());
            examDto.setStartTime(exam.getStartTime());
            examDto.setCompleted(exam.getCompleted());

            // Group nomini qo‘shamiz
            if (exam.getGroup() != null) {
                examDto.setGroupName(exam.getGroup().getName());
            } else {
                examDto.setGroupName(null);
            }

            // Studentning shu examdagi baholari
            List<ExamGrades> grades = examGradesRepo.findByExamAndStudent(exam, user);

            if(grades.isEmpty()) {
                List<ExamStudentMarkResDto> collect = exam.getExamTypes().stream().map(t -> {
                    ExamStudentMarkResDto dto = new ExamStudentMarkResDto();
                    dto.setId(t.getId());
                    dto.setTypeName(t.getName());
                    dto.setMark(null);
                    return dto;
                }).collect(Collectors.toList());
                examDto.setMarks(collect);
            }else {
                List<ExamStudentMarkResDto> marks = grades.stream().map(g -> {
                    ExamStudentMarkResDto dto = new ExamStudentMarkResDto();
                    dto.setId(g.getId());
                    dto.setTypeName(g.getTypeName());
                    dto.setMark(g.getScore());
                    return dto;
                }).collect(Collectors.toList());
                examDto.setMarks(marks);
            }


            studentRatings.add(examDto);
        }

        return studentRatings;
    }



    @Transactional
    @Override
    public void addExam(UUID groupId, ExamDto examDto) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        Exam exam = new Exam();
        exam.setTitle(examDto.getTitle());
        exam.setStartTime(LocalTime.parse(examDto.getTime()));
        exam.setDate(LocalDate.parse(examDto.getDate()));
        exam.setGroup(group);
        exam.setCompleted(false);

        List<ExamTypes> examTypes = examDto.getTypeIds().stream()
                .map(typeId -> examTypesRepo.findById(typeId)
                        .orElseThrow(() -> new RuntimeException("ExamType not found with id: " + typeId)))
                .collect(Collectors.toList());

        exam.setExamTypes(examTypes);

        examRepo.save(exam);
    }





}
