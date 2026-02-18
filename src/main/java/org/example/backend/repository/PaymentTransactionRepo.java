package org.example.backend.repository;

import org.example.backend.entity.Payment;
import org.example.backend.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepo extends JpaRepository<PaymentTransaction, UUID> {

    List<PaymentTransaction> findByPayment(Payment payment);
}
