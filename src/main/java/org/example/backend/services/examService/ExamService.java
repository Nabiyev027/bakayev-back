package org.example.backend.services.examService;

import org.example.backend.dto.ExamDto;
import org.example.backend.dto.StudentMarkDto;
import org.example.backend.dtoResponse.*;

import java.util.List;
import java.util.UUID;

public interface ExamService {

    void addExam(UUID groupId, ExamDto examDto);

    List<ExamResDto> getExams(UUID groupId);


    void editExam(UUID examId, ExamDto examDto);

    void delExam(UUID id);

    List<ExamTypeResDto> getExamTypes(UUID examId);

    List<ExamGradeResDto> getExamGradeStudent(UUID examId);

    void markStudents(UUID examId, List<StudentMarkDto> studentMarks);

    List<ExamUserRatingResDto> getStudentRatings(UUID studentId);
}
