package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity(name = "filial")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Filial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String location;

    @NotBlank
    private String imageUrl;

    @OneToMany(mappedBy = "filial", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Room> rooms;

    @ManyToMany(mappedBy = "filials")
    private List<User> users;

    @OneToMany(mappedBy = "filial", cascade = CascadeType.ALL)
    private List<Group> groups;

}

