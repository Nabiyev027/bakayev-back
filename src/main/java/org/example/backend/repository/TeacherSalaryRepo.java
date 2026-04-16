package org.example.backend.repository;

import org.example.backend.entity.TeacherSalary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TeacherSalaryRepo extends JpaRepository<TeacherSalary, UUID> {
    
    @Query("SELECT ts FROM TeacherSalary ts WHERE ts.teacher.id = :teacherId AND ts.salaryDate >= :startDate AND ts.salaryDate <= :endDate")
    List<TeacherSalary> findByTeacherAndDateRange(UUID teacherId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT ts FROM TeacherSalary ts WHERE ts.teacher.id = :teacherId AND ts.group.id = :groupId AND ts.salaryDate >= :startDate AND ts.salaryDate <= :endDate")
    List<TeacherSalary> findByTeacherAndGroupAndDateRange(UUID teacherId, UUID groupId, LocalDate startDate, LocalDate endDate);
}
