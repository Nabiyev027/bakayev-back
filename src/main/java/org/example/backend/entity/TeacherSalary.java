package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TeacherSalary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    // Shu oy shu guruhdan o'qituvchi jami qancha olishi kerak
    private Integer totalAmount;

    // Qaysi oy uchun ekanligi
    private LocalDate salaryDate;

    // Shu guruhdan o'qituvchi necha foiz oladi
    @Column(nullable = false)
    private Integer percentage = 0;

    // Shu oy va shu guruh maoshidan qilingan qisman to'lovlar
    @OneToMany(mappedBy = "teacherSalary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherSalaryPayment> payments;
}
