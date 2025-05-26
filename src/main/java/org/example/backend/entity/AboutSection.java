package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.Enum.Lang;

import java.util.List;
import java.util.UUID;

@Entity(name = "about_section")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AboutSection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @NotBlank
    @Column(nullable = false)
    private String imgUrl;

    @NotBlank
    @Column(nullable = false)
    private String videoUrl;


    @OneToMany(mappedBy = "aboutSection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AboutSectionTranslation> translations;

}
