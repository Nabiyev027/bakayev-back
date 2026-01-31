package org.example.backend.repository;

import org.example.backend.entity.ReceptionSalary;
import org.example.backend.entity.ReceptionSalaryPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReceptionSalaryPaymentRepo extends JpaRepository<ReceptionSalaryPayment, UUID> {

}
