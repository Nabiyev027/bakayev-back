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
public class ReceptionSalary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reception_id", nullable = false)
    private User receptionist;

    private Integer salaryAmount;

    private LocalDate salaryDate;

    @OneToMany(mappedBy = "receptionSalary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceptionSalaryPayment> payments;

}

