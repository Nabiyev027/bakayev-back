package org.example.backend.repository;

import org.example.backend.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GroupRepo extends JpaRepository<Group, UUID> {

}
