package org.example.backend.repository;

import org.example.backend.entity.Debts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DebtsRepo extends JpaRepository<Debts, UUID> {
}
