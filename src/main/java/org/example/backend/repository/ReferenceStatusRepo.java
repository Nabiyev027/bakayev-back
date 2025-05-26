package org.example.backend.repository;

import org.example.backend.entity.Reference;
import org.example.backend.entity.ReferenceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReferenceStatusRepo extends JpaRepository<ReferenceStatus, UUID> {
    ReferenceStatus findByReferenceId(UUID referenceId);
}
