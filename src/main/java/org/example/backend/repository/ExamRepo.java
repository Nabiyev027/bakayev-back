package org.example.backend.repository;

import org.example.backend.entity.Exam;
import org.example.backend.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExamRepo extends JpaRepository<Exam, UUID> {

    List<Exam> findAllByGroup(Group group);

    List<Exam> findByGroupIn(List<Group> groups);

}
