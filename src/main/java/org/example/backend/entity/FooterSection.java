package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "footer_section")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FooterSection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String phone1;
    @NotBlank
    @Column(nullable = false)
    private String phone2;

    @NotBlank
    @Column(nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String instagramUrl;
    @NotBlank
    @Column(nullable = false)
    private String telegramUrl;

    private String facebookUrl;

}
