package org.example.backend.repository;

import org.example.backend.entity.ReceptionSalary;
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

    // Barcha filiallar uchun
    @Query("""
                select s from ReceptionSalary s
                where s.salaryDate between :startDate and :endDate
            """)
    List<ReceptionSalary> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Tanlangan filial uchun
    @Query("""
                select s from ReceptionSalary s
                join s.receptionist r
                join r.filials f
                where f.id = :filialId
                and s.salaryDate between :startDate and :endDate
            """)
    List<ReceptionSalary> findByFilialAndDateRange(
            @Param("filialId") UUID filialId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT s FROM ReceptionSalary s
            WHERE s.receptionist.id = :receptionId
            AND s.salaryDate BETWEEN :start AND :end
            """)
    Optional<ReceptionSalary> findByReceptionAndMonth(
            @Param("receptionId") UUID receptionId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );


    Optional<ReceptionSalary> findTopByReceptionistIdAndSalaryDateBeforeOrderBySalaryDateDesc(UUID id, LocalDate startDate);
}
