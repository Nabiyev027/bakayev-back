package org.example.backend.repository;

import org.example.backend.dtoResponse.StudentProjection;
import org.example.backend.entity.Filial;
import org.example.backend.entity.Group;
import org.example.backend.entity.Role;
import org.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r IN :roles")
    List<User> getByRoles(@Param("roles") List<Role> roles);

    @Transactional(readOnly = true)
    @Query("""
    SELECT u
    FROM User u
    JOIN u.groupStudents gs
    WHERE gs.group = :group
      AND :role MEMBER OF u.roles
""")
    List<StudentProjection> findUsersByGroupAndRole(
            @Param("group") Group group,
            @Param("role") Role role
    );


    @Query("""
                SELECT u FROM User u
                JOIN u.roles r
                JOIN u.groupStudents gs
                WHERE r.name = 'ROLE_STUDENT'
                  AND gs.group = :group
                  AND :filial member of u.filials
            """)
    List<User> findStudentsByFilialAndGroup(@Param("filial") Filial filial,
                                            @Param("group") Group group);


    @Transactional(readOnly = true)
    @Query("""
                SELECT u FROM User u
                JOIN u.roles r
                JOIN u.groupStudents gs
                WHERE r.name = 'ROLE_STUDENT'
                  AND gs.group = :group
            """)
    List<User> findStudentsByGroup(@Param("group") Group group);


    @Transactional(readOnly = true)
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.teacherGroups WHERE u.id = :id")
    Optional<User> findByIdWithGroups(@Param("id") UUID id);

    @Transactional(readOnly = true)
    @Query("""
                SELECT u FROM User u
                JOIN u.roles r
                WHERE r.name = 'ROLE_STUDENT'
                  AND :filial MEMBER OF u.filials
            """)
    List<User> findStudentsByFilial(@Param("filial") Filial filial);

    boolean existsByUsername(String username);

    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            WHERE r.name = :roleName
            """)
    List<User> findAllByRole(
            @Param("roleName") String roleName
    );


    @Query("""
            SELECT u FROM User u
            JOIN u.roles r
            JOIN u.filials f
            WHERE r.name = :roleName
            AND f.id = :filialId
            """)
    List<User> findAllByRoleAndFilial(
            @Param("roleName") String roleName,
            @Param("filialId") UUID filialId
    );

}
