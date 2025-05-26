package org.example.backend.services.notificationService;

import lombok.AllArgsConstructor;
import okhttp3.*;
import org.example.backend.dto.NotificationDto;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void postMessage(NotificationDto notificationDto) throws IOException {

    }

}
