package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.Enum.GroupStudentStatus;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupStudent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Enumerated(EnumType.STRING)
    private GroupStudentStatus status = GroupStudentStatus.ACTIVE;
}
