package org.example.backend.repository;

import org.example.backend.entity.Discount;
import org.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiscountRepo extends JpaRepository<Discount, UUID> {

    Optional<Discount> findByStudentAndActiveTrue(User student);

    List<Discount> getDiscountsByStudent(User student);

    Discount findTopByStudentAndActiveOrderByEndDateDesc(User user, boolean b);
}
