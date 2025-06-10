package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity(name = "card_skill")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_card_id", nullable = false)
    private CourseCard courseCard;

    @OneToMany(mappedBy = "cardSkill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CardSkillTranslation> translations;
}
