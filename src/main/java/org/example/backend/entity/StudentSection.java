package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity(name = "student_section")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentSection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String imgUrl;

    @NotBlank
    @Column(nullable = false)
    private String name;


    @Column(nullable = false)
    private Double listening;


    @Column(nullable = false)
    private Double reading;


    @Column(nullable = false)
    private Double writing;


    @Column(nullable = false)
    private Double speaking;

    @Column(nullable = false)
    private Double overall;


}
