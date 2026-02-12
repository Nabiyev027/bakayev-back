package org.example.backend.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class NotificationDto {
    private List<String> studentsId;
    private boolean msgToStudent;
    private boolean msgToParent;
    private UUID messageTextId;
}
