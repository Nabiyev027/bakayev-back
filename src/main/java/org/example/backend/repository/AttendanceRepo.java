package org.example.backend.repository;

import org.example.backend.entity.Attendance;
import org.example.backend.entity.Group;
import org.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepo extends JpaRepository<Attendance, UUID> {
    List<Attendance> getAttendancesByGroup(Group group);


    Optional<Attendance> findByGroupAndStudentAndDate(Group group, User student, LocalDate today);
    List<Attendance> findByGroupAndDate(Group group, LocalDate today);
}
