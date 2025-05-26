package org.example.backend.repository;

import org.example.backend.entity.Filial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FilialRepo extends JpaRepository<Filial, UUID> {
}
