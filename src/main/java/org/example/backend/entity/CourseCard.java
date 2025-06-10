package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity(name = "course_card")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseCard {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_section_id", nullable = false)
    private CourseSection courseSection;

    @OneToMany(mappedBy = "courseCard", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CardSkill> cardSkills;

    @OneToMany(mappedBy = "courseCard", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CourseCardTranslation> translations;


}
