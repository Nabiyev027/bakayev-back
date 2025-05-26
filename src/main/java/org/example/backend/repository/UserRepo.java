package org.example.backend.repository;

import org.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    @Query(value = """
    SELECT u.*
    FROM users u
    JOIN group_students gs ON u.id = gs.student_id
    WHERE gs.group_id = :groupId
    """, nativeQuery = true)
    List<User> getByGroupId(@Param("groupId") UUID groupId);
}
