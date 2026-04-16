package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TeacherSalaryPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Ushbu qisman to'lovning summasi
    private Integer amount;

    // To'lov qilingan sana
    private LocalDate paymentDate;

    // Qaysi oylik umumiy maoshga tegishli ekanligi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_salary_id", nullable = false)
    private TeacherSalary teacherSalary;
}
