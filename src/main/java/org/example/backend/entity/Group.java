package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity(name = "groups")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @NotBlank
    private String name;
    @NotBlank
    private String degree;

    private LocalTime startTime;
    private LocalTime endTime;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<Lesson> lessons = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    private Room room;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "group_teachers",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    private List<User> teachers;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "group_students",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<User> students;


}
