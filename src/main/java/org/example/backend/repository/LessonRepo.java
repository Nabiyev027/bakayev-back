package org.example.backend.repository;

import org.example.backend.entity.Group;
import org.example.backend.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepo extends JpaRepository<Lesson, UUID> {

    List<Lesson> getByGroup(Group group);

    Optional<Lesson> findByGroupIdAndDate(UUID groupId, LocalDate date);

    @Query(
            value = """
        SELECT 
            g.id as group_id,
            g.start_time,
            g.end_time,
            u.id as student_id,
            CONCAT(u.first_name, ' ', u.last_name) as student_name,
            lm.id as mark_id,
            lm.type_name,
            lm.mark
        FROM groups g
        JOIN group_students sg ON sg.group_id = g.id
        JOIN users u ON u.id = sg.student_id
        LEFT JOIN lessons l ON l.group_id = g.id 
            AND l.date = CURRENT_DATE
        LEFT JOIN lesson_marks lm ON lm.lesson_id = l.id 
            AND lm.student_id = u.id
        WHERE g.id = :groupId
        """,
            nativeQuery = true
    )
    List<Object[]> getLessonGroupWithStudents(@Param("groupId") UUID groupId);


}
