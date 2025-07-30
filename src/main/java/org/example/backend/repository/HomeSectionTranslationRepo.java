package org.example.backend.repository;

import org.example.backend.Enum.Lang;
import org.example.backend.entity.HomeSectionTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HomeSectionTranslationRepo extends JpaRepository<HomeSectionTranslation, UUID> {
    Optional<HomeSectionTranslation> findByHomeSectionIdAndLanguage(UUID homeSection_id, Lang language);
}
