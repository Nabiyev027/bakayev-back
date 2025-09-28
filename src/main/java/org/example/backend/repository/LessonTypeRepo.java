package org.example.backend.repository;

import org.example.backend.entity.LessonTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LessonTypeRepo extends JpaRepository<LessonTypes, UUID> {

    boolean existsByNameIgnoreCase(String name);
}
