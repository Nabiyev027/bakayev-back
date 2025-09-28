package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.Enum.Lang;

import java.util.UUID;

@Entity
@Table(name = "about_section_translation")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AboutSectionTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false, length = 3000)
    private String description1;

    @NotBlank
    @Column(nullable = false, length = 3000)
    private String description2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Lang language; // UZ, RU, EN

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "about_section_id", nullable = false)
    private AboutSection aboutSection;

}
