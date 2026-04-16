package org.example.backend.repository;

import org.example.backend.entity.TeacherSalary;
import org.example.backend.entity.TeacherSalaryPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TeacherSalaryPaymentRepo extends JpaRepository<TeacherSalaryPayment, UUID> {
}
