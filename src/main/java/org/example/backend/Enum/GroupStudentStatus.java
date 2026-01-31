package org.example.backend.Enum;

public enum GroupStudentStatus {
    ACTIVE,
    LEFT,
    GRADUATED;

    @Override
    public String toString() {
        return name();
    }

}
