package org.example.backend.services.notificationService;


import org.example.backend.dto.NotificationDto;

import java.io.IOException;

public interface NotificationService {

    void sendMessageToStudentsOrParents(NotificationDto notificationDto) throws IOException;
}
