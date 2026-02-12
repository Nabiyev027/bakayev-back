package org.example.backend.entity;

import jakarta.persistence.*;
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

    private String phone1;
    private String phone2;
    private String email;
    private String instagramUrl;
    private String telegramUrl;
    private String facebookUrl;

}
