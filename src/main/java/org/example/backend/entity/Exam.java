package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Entity(name = "exam")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String title;

    private LocalDate date;
    private LocalTime startTime;

    private Boolean completed;

    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamGrades> examGrades;

    @ManyToMany
    @JoinTable(
            name = "exam_types_of_exam", // oraliq jadval avtomatik yaratiladi
            joinColumns = @JoinColumn(name = "exam_id"),
            inverseJoinColumns = @JoinColumn(name = "exam_type_id")
    )
    private List<ExamTypes> examTypes;  // ✅ bitta examda bir nechta type

}
















