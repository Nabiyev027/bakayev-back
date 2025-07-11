package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity(name = "users")
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
    @NotBlank
    private String phone;

    private String parentPhone;
    @NotBlank
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

    @ManyToMany(mappedBy = "students")
    private List<Group> studentGroups;

    @ManyToMany(mappedBy = "teachers")
    private List<Group> teacherGroups;


    @ManyToOne
    @JoinColumn(name = "filial_id")
    private Filial filial;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="teacher_students", joinColumns = @JoinColumn(name = "teacher_id"), inverseJoinColumns = @JoinColumn(name = "student_id"))
    @ToString.Exclude
    private Set<User> students = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "students")
    @ToString.Exclude
    private Set<User> teachers = new HashSet<>();

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
