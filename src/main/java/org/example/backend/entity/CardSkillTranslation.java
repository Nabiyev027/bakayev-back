package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.Enum.Lang;

import java.util.UUID;

@Entity(name = "card_skill_translation")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardSkillTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @NotNull
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Lang language; // UZ, RU, EN

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_skill_id", nullable = false)
    private CardSkill cardSkill;
}
