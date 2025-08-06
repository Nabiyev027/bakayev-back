package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity(name = "teacher_section")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherSection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String imgUrl;

    @NotBlank
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Column(nullable = false)
    private String lastName;

    @NotBlank
    @NotNull
    private String ieltsBall;
    @NotNull
    private String certificate;
    @NotNull
    private Integer experience;
    @NotNull
    private Integer numberOfStudents;

    @OneToMany(mappedBy = "teacherSection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TeacherSectionTranslation> translations;

}
