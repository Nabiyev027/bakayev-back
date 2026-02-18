package org.example.backend.repository;

import org.example.backend.Enum.DayType;
import org.example.backend.entity.Filial;
import org.example.backend.entity.Group;
import org.example.backend.entity.Room;
import org.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    List<Group> getGroupByFilial(Filial filial, Sort sort);


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
             JOIN group_student gs ON g.id = gs.group_id
             JOIN users s ON s.id = gs.student_id
    WHERE s.id = :studentId
    """, nativeQuery = true)
    List<Group> getGroupsByStudent(@Param("studentId") UUID studentId);

    List<Group> findByFilialIdIn(List<String> filialIds);

    List<Group> findByRoom(Room room);

    List<Group> findAllByDayTypeAndRoom(DayType dayType, Room room);

    Page<Group> findByFilial(Filial filial, Pageable pageable);
}
