package org.example.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "payment_course_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCourseInfo {
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private UUID id;

    @NotNull
    private Integer paymentDay;

    @NotNull
    private Integer coursePaymentAmount;

}
