package org.example.backend.repository;

import org.example.backend.entity.TeacherSalary;
import org.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherSalaryRepo extends JpaRepository<TeacherSalary, UUID> {


    List<TeacherSalary> findAllByTeacherAndSalaryDateBetween(
            User teacher,
            LocalDate start,
            LocalDate end
    );


    @Query("""
    SELECT s FROM TeacherSalary s
    WHERE s.teacher.id = :teacherId
    AND s.salaryDate BETWEEN :start AND :end
    ORDER BY s.salaryDate DESC
""")
    List<TeacherSalary> findAllByTeacherAndMonth(
            @Param("teacherId") UUID teacherId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );


    Optional<TeacherSalary> findTopByTeacherIdAndSalaryDateBeforeOrderBySalaryDateDesc(UUID id, LocalDate startDate);
}
