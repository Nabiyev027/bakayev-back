package org.example.backend.repository;

import org.example.backend.entity.Filial;
import org.example.backend.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


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
    WHERE t.id = '65a37a00-3b8d-4584-a624-b06438e79d02'
    """, nativeQuery = true)
    List<Group> getGroupsByTeacher(@Param("teacherId") UUID teacherId);

}
