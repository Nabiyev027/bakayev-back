package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity(name = "grade")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @NotNull
    private Integer score;
    @NotBlank
    private Date date;
    @NotBlank
    private String comment;

    @ManyToOne(fetch = FetchType.EAGER)
    private User student;
    @ManyToOne(fetch = FetchType.EAGER)
    private Group group;
    @ManyToOne(fetch = FetchType.EAGER)
    private Lesson lesson;
}
