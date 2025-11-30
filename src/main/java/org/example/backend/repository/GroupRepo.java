package org.example.backend.repository;

import org.example.backend.entity.Filial;
import org.example.backend.entity.Group;
import org.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupRepo extends JpaRepository<Group, UUID> {
    Optional<Group> findFirstByOrderByIdAsc();

    List<Group> getGroupByFilial(Filial filial);

    @Query(value = """
    SELECT g.*
    FROM groups g
             JOIN group_teachers gt ON g.id = gt.group_id
             JOIN users t ON t.id = gt.teacher_id
    WHERE t.id = :teacherId
    """, nativeQuery = true)
    List<Group> getGroupsByTeacher(@Param("teacherId") UUID teacherId);


    @Query(value = """
    SELECT g.*
    FROM groups g
             JOIN group_students gs ON g.id = gs.group_id
             JOIN users s ON s.id = gs.student_id
    WHERE s.id = :studentId
    """, nativeQuery = true)
    List<Group> getGroupsByStudent(@Param("studentId") UUID studentId);

    List<Group> findByFilialIdIn(List<String> filialIds);

}
