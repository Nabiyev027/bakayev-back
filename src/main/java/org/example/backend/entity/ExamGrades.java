package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private Integer score;

    @NotBlank 
    private String typeName;

    @ManyToOne(fetch = FetchType.LAZY)
    private User student;  // Kimga qo‘yilgan

    @ManyToOne(fetch = FetchType.LAZY)
    private Exam exam;        // Qaysi imtihon

}
