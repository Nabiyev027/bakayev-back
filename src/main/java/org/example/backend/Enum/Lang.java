package org.example.backend.Enum;


public enum Lang {
    EN,
    UZ,
    RU;

    @Override
    public String toString() {
        return name().toLowerCase(); // en, uz, ru
    }
}
