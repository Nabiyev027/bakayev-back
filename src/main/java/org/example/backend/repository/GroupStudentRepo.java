package org.example.backend.repository;

import org.example.backend.entity.GroupStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupStudentRepo extends JpaRepository<GroupStudent, UUID> {

    @Query("""
        SELECT gs FROM GroupStudent gs
        JOIN FETCH gs.student s
        WHERE gs.group.id = :groupId
    """)
    List<GroupStudent> findByGroup(UUID groupId);
}
