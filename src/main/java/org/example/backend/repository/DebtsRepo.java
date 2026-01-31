package org.example.backend.repository;

import org.example.backend.entity.Debts;
import org.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DebtsRepo extends JpaRepository<Debts, UUID> {

    Optional<Debts> findByStudent(User student);

    // Foydalanuvchining qarzini o‘chirish uchun
    void deleteByStudent(User student);

    List<Debts> findByStudentOrderByCreatedDateAsc(User user);
}
