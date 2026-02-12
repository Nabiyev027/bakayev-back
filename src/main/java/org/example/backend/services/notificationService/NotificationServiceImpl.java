package org.example.backend.services.notificationService;

import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.example.backend.controller.EskizAuth;
import org.example.backend.dto.NotificationDto;
import org.example.backend.entity.MessageText;
import org.example.backend.entity.User;
import org.example.backend.repository.MessageTextRepo;
import org.example.backend.repository.UserRepo;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final UserRepo userRepo;
    private final MessageTextRepo messageRepo; // Xabar matnini olish uchun repo qo'shdik

    @Override
    public void sendMessageToStudentsOrParents(NotificationDto notificationDto) throws IOException {
        // 1. Eskiz tokenini olish
        String token = EskizAuth.getToken();

        // 2. Bazadan xabar matnini ID orqali topish
        MessageText messageEntity = messageRepo.findById(notificationDto.getMessageTextId())
                .orElseThrow(() -> new RuntimeException("Not found text message!"));

        String finalText = messageEntity.getDescription(); // Bazadagi matn

        OkHttpClient client = new OkHttpClient();

        // 3. Har bir tanlangan talaba uchun aylanamiz
        notificationDto.getStudentsId().forEach(studentId -> {
            User student = userRepo.findById(UUID.fromString(studentId))
                    .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

            try {
                // Talabaga yuborish (agar checkbox belgilangan bo'lsa)
                if (notificationDto.isMsgToStudent() && student.getPhone() != null) {
                    sendSms(client, token, student.getPhone(), finalText);
                }

                // Ota-onasiga yuborish (agar checkbox belgilangan bo'lsa)
                if (notificationDto.isMsgToParent() && student.getParentPhone() != null) {
                    sendSms(client, token, student.getParentPhone(), finalText);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void sendSms(OkHttpClient client, String token, String phone, String message) throws IOException {
        if (message == null || message.isEmpty()) return;

        // Telefon raqamdan faqat raqamlarni qoldirish (+99890... -> 99890...)
        String formattedPhone = phone.replaceAll("\\D", "");

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("from", "4546") // Eskiz dan berilgan kod
                .addFormDataPart("mobile_phone", formattedPhone)
                .addFormDataPart("message", message) // Endi bu yerda haqiqiy xabar matni
                .addFormDataPart("callback_url", "http://0000.uz/test.php")
                .build();

        Request request = new Request.Builder()
                .url("https://notify.eskiz.uz/api/message/sms/send")
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Noma'lum xato";
                System.out.println("Error to post message (" + phone + "): " + errorBody);
            }
        }
    }
}
