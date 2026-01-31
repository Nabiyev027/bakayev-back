package org.example.backend.repository;

import org.example.backend.entity.Group;
import org.example.backend.entity.TeacherSalary;
import org.example.backend.entity.TeacherSalaryPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TeacherSalaryPaymentRepo extends JpaRepository<TeacherSalaryPayment, UUID> {

    @Query("""
    select coalesce(sum(p.amount), 0)
    from TeacherSalaryPayment p
    where p.teacherSalary = :salary
      and p.teacherSalary.group = :group
""")
    Integer sumAmountByTeacherSalaryAndGroup(
            @Param("salary") TeacherSalary salary,
            @Param("group") Group group
    );


}
