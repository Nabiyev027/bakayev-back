package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity(name = "lesson_marks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonMarks {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private Integer mark;

    @NotNull
    private String typeName;

    @ManyToOne
    @JoinColumn(name = "lesson_id") // foreign key
    private Lesson lesson;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

}
