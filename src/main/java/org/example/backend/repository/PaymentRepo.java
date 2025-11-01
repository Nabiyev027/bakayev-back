package org.example.backend.repository;

import org.example.backend.Enum.PaymentStatus;
import org.example.backend.entity.Payment;
import org.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, UUID> {

    List<Payment> getPaymentByStudent(User student);

    Payment getPaymentByStudentAndPaymentStatus(User user, PaymentStatus paymentStatus);

    List<Payment> findPaymentsByStudent(User student);
}
