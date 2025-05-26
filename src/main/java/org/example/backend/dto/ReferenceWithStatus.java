package org.example.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReferenceWithStatus {
    private String id;
    private String name;
    private String phone;
    private String telegramUserName;
    private String receptionName;
    private boolean status;
    private LocalDateTime calledDateTime;

}
