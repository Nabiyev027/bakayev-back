package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "attendance")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @NotNull
    private LocalDate date;
    @NotNull
    private Boolean status;
    private String cause;
    @ManyToOne(fetch = FetchType.EAGER)
    private User student;
    @ManyToOne(fetch = FetchType.EAGER)
    private Group group;

}
