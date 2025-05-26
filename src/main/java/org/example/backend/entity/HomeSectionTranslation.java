package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.Enum.Lang;

import java.util.UUID;

@Entity
@Table(name = "home_section_translation")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomeSectionTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Lang language; // UZ, RU, EN

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_section_id", nullable = false)
    private HomeSection homeSection;
}
