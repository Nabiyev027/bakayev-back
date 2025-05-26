package org.example.backend.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.Enum.Lang;

import java.util.UUID;

@Entity(name = "difference_section_translation")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DifferenceSectionTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Lang language; // UZ, RU, EN

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "difference_section_id", nullable = false)
    private DifferenceSection differenceSection;

}
