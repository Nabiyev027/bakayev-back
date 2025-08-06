package org.example.backend.repository;

import org.example.backend.dtoResponse.StudentProjection;
import org.example.backend.dtoResponse.StudentResDto;
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

    List<User> getByRoles(List<Role> roles);

    @Transactional
    @Query("""
    SELECT u FROM users u WHERE :group member of u.studentGroups and :role member of u.roles
""")
    List<StudentProjection> findUsersByGroupAndRole(@Param("group") Group group, @Param("role") Role role);


}
