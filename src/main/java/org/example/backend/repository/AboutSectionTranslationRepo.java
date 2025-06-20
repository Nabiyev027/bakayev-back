package org.example.backend.repository;

import org.example.backend.entity.AboutSectionTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AboutSectionTranslationRepo extends JpaRepository<AboutSectionTranslation, UUID> {
}
