package org.example.backend.repository;

import org.example.backend.entity.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseSectionRepo extends JpaRepository<CourseSection, UUID> {

//    @Query("SELECT DISTINCT cs FROM CourseSection cs " +
//            "LEFT JOIN FETCH cs.translations t " +
//            "LEFT JOIN FETCH cs.courseCards cc " +
//            "LEFT JOIN FETCH cc.translations ct")
//    List<CourseSection> findAllWithTranslationsAndCards();
}
