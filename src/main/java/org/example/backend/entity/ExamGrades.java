package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity(name = "exam_grades")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamGrades {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private Integer score;

    @ManyToOne(fetch = FetchType.EAGER)
    private Exam exam;

    @ManyToOne(fetch = FetchType.EAGER)
    private User student;
}
