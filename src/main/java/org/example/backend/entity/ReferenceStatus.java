package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "referenceId", nullable = false, unique = true)
    private Reference reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receptionId", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User receptionist;

    private boolean status;
    private LocalDateTime calledDateTime;
}
