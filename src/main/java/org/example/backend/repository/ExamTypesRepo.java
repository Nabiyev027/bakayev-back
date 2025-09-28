package org.example.backend.repository;

import org.example.backend.entity.ExamTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExamTypesRepo extends JpaRepository<ExamTypes, UUID> {
    boolean existsByNameIgnoreCase(String name);
}
