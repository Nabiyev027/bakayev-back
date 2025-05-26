package org.example.backend.services.notificationService;


import org.example.backend.dto.NotificationDto;

import java.io.IOException;

public interface NotificationService {
    void postMessage(NotificationDto notificationDto) throws IOException;

}
