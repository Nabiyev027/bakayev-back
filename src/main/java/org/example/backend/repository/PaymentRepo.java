package org.example.backend.repository;

import org.example.backend.Enum.PaymentStatus;
import org.example.backend.entity.Payment;
import org.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, UUID> {

    Payment getPaymentByStudentAndPaymentStatus(User user, PaymentStatus paymentStatus);

    List<Payment> findPaymentsByStudent(User student);

    Payment getLastPaymentByStudent(User user);

    @Query("SELECT p FROM payment p WHERE p.student = :student AND p.paymentStatus = :status ORDER BY p.date DESC")
    Optional<Payment> getFirstPendingPayment(@Param("student") User student, @Param("status") PaymentStatus status);

}
