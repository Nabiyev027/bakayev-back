package org.example.backend.repository;

import jakarta.validation.constraints.NotNull;
import org.example.backend.entity.Exam;
import org.example.backend.entity.ExamGrades;
import org.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamGradesRepo extends JpaRepository<ExamGrades, UUID> {

    Optional<ExamGrades> findByExamAndStudentAndTypeName(Exam exam, User student, @NotNull String name);


    List<ExamGrades> findByExamAndStudent(Exam exam, User student);
}
