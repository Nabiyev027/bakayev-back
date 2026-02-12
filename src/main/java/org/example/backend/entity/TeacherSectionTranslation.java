package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.Enum.Lang;

import java.util.UUID;

@Entity(name = "teacher_section_translation")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherSectionTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Lang language; // UZ, RU, EN

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_section_id", nullable = false)
    private TeacherSection teacherSection;

}
