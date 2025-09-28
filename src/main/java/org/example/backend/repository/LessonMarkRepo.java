package org.example.backend.repository;
import org.example.backend.entity.LessonMarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonMarkRepo extends JpaRepository<LessonMarks, UUID> {

    Optional<LessonMarks> findByLessonIdAndStudentIdAndTypeName(UUID lessonId, UUID studentId, String typeName);

}
