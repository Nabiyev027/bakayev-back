package org.example.backend.repository;

import org.example.backend.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RefundRepo extends JpaRepository<Refund, UUID> {

    @Query("""
        select r from Refund r
        join r.student s
        join s.groupStudents gs
        join gs.group g
        join g.teachers t
        join g.filial f
        where (:filialId is null or f.id = :filialId)
          and (:teacherId is null or t.id = :teacherId)
          and (:groupId is null or g.id = :groupId)
          and (:studentId is null or s.id = :studentId)
    """)
    List<Refund> findRefunds(
            @Param("filialId") UUID filialId,
            @Param("teacherId") UUID teacherId,
            @Param("groupId") UUID groupId,
            @Param("studentId") UUID studentId
    );

}
