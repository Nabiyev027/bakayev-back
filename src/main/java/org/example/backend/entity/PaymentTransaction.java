package org.example.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity(name = "payment_transaction")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @NotNull
    private double amount;
    @NotNull
    private Date transactionDate;
    @NotBlank
    private String method;
    @ManyToOne(fetch = FetchType.EAGER)
    private Payment payment;
}
