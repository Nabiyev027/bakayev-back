package org.example.backend.repository;

import org.example.backend.entity.CourseCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseCardRepo extends JpaRepository<CourseCard, UUID> {
    List<CourseCard> findAllByCourseSectionId(UUID courseSectionId);
}
