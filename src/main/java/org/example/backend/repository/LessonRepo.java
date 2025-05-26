package org.example.backend.repository;

import org.example.backend.entity.Group;
import org.example.backend.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonRepo extends JpaRepository<Lesson, UUID> {

    List<Lesson> getByGroup(Group group);
}
