package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity(name = "about_section")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AboutSection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = true)
    private String imgUrl;

    @Column(nullable = true)
    private String videoUrl;

    @Column(nullable = true)
    private String videoThumbnailUrl;

    @NotNull
    @Min(0)
    private Integer successfulStudents;
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Double averageScore;
    @NotNull
    @Min(0)
    private Integer yearsExperience;
    @NotNull
    @Min(0)
    private Integer successRate;


    @OneToMany(mappedBy = "aboutSection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AboutSectionTranslation> translations;

}
