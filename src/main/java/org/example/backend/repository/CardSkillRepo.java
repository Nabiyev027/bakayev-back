package org.example.backend.repository;

import org.example.backend.entity.CardSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardSkillRepo extends JpaRepository<CardSkill, UUID> {

}
