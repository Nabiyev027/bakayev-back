package org.example.backend.repository;

import org.example.backend.entity.StudentSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StudentSectionRepo extends JpaRepository<StudentSection, UUID> {
}
