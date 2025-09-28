package org.example.backend.repository;

import org.example.backend.entity.PaymentCourseInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentCourseInfoRepo extends JpaRepository<PaymentCourseInfo, UUID> {

    Optional<PaymentCourseInfo> findFirstBy();

}
