package org.example.backend.Enum;

public enum PaymentStatus {
    PENDING,
    PAID;

    @Override
    public String toString() {
        return name(); // en, uz, ru
    }

}
