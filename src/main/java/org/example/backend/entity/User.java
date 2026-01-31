package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;

    private String phone;

    private String parentPhone;
    @NotBlank
    @Column(unique = true)
    private String username;
    @NotBlank
    private String password;

    private String imageUrl;

    @OneToMany(mappedBy = "receptionist")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<ReferenceStatus> referenceStatuses;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Role> roles;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupStudent> groupStudents;

    @ManyToMany(mappedBy = "teachers")
    private List<Group> teacherGroups;

    @Column(nullable = false)
    private boolean status = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_filial",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "filial_id")
    )
    private List<Filial> filials;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="teacher_students", joinColumns = @JoinColumn(name = "teacher_id"), inverseJoinColumns = @JoinColumn(name = "student_id"))
    @ToString.Exclude
    private Set<User> students = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "students")
    @ToString.Exclude
    private Set<User> teachers = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Debts> debts;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Discount> discounts = new ArrayList<>();

    // Student sifatida olingan refundlar
    @OneToMany(mappedBy = "student")
    private List<Refund> studentRefunds;

    // Receptionist amalga oshirgan refundlar
    @OneToMany(mappedBy = "receptionist")
    private List<Refund> receptionistRefunds;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamGrades> examGrades = new ArrayList<>();


    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherSalary> teacherSalaries = new ArrayList<>();

    @OneToMany(mappedBy = "receptionist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceptionSalary> receptionSalaries = new ArrayList<>();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getUsername() {
        return username;
    }
    @Override
    public String getPassword() {
        return password;
    }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }

}
