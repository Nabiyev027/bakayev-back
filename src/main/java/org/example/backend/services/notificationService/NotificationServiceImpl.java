package org.example.backend.services.notificationService;

import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.example.backend.controller.EskizAuth;
import org.example.backend.dto.NotificationDto;
import org.example.backend.entity.User;
import org.example.backend.repository.UserRepo;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final String TOKEN_FILE_PATH = "src/main/resources/token.txt";
    private final UserRepo userRepo;


    @Override
    public void sendMessageToStudentsOrParents(NotificationDto notificationDto) throws IOException {
        // Tokenni har safar oling (EskizAuth.getToken() tokenni tekshiradi va yangilaydi)
        String token = EskizAuth.getToken();

        OkHttpClient client = new OkHttpClient();

        notificationDto.getStudentsId().forEach(studentId -> {
            User student = userRepo.findById(UUID.fromString(studentId))
                    .orElseThrow(() -> new RuntimeException("Talaba topilmadi: " + studentId));

            try {
                if (student.getPhone() != null)
                    sendSms(client, token, student.getPhone(), notificationDto.getReportStudent());

                if (student.getParentPhone() != null)
                    sendSms(client, token, student.getParentPhone(), notificationDto.getReportParent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    private void sendSms(OkHttpClient client, String token, String phone, String message) throws IOException {
        if (message == null || message.isEmpty()) return;

        String formattedPhone = phone.replaceAll("\\D", "");

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("from", "4546")
                .addFormDataPart("mobile_phone", formattedPhone)
                .addFormDataPart("message", "Bu Eskiz dan test") // asl xabar
                .addFormDataPart("callback_url", "http://0000.uz/test.php")
                .build();

        Request request = new Request.Builder()
                .url("https://notify.eskiz.uz/api/message/sms/send")
                .post(body)
                .addHeader("Authorization", "Bearer " + token) // token tozalangan
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("SMS yuborishda xatolik: " + phone + " - " + response.body().string());
            }
        }
    }




}
