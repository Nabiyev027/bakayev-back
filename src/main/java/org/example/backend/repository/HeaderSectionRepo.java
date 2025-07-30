package org.example.backend.repository;

import org.example.backend.entity.HomeSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HeaderSectionRepo extends JpaRepository<HomeSection, UUID> {
    Optional<HomeSection> findTopByOrderByIdAsc();
}
