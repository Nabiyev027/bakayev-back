package org.example.backend.repository;

import org.example.backend.entity.ReceptionSalary;
import org.example.backend.entity.TeacherSalary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReceptionSalaryRepo extends JpaRepository<ReceptionSalary, UUID> {

    @Query("""
    SELECT s FROM ReceptionSalary s
    WHERE s.receptionist.id = :receptionId
    AND s.salaryDate BETWEEN :startDate AND :endDate
""")
    Optional<ReceptionSalary> findByReceptionAndDateRange(
            @Param("receptionId") UUID receptionId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
