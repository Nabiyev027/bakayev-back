package org.example.backend.controller;

import okhttp3.*;

import java.io.IOException;

public class EskizSmsSender {

    public static void main(String[] args) throws IOException {
        // 1. Eskizdan olingan tokenni bu yerga yozing yoki fayldan o‘qing
        String token = "SIZNING_TOKENINGIZ"; // Masalan: "eyJ0eXAiOiJKV1QiLCJhbGciOi..."

        OkHttpClient client = new OkHttpClient();

        // 2. Request bodyni yaratamiz
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("mobile_phone", "998934410412") // Telefon raqami
                .addFormDataPart("message", "Eskiz test xabari") // Xabar matni
                .addFormDataPart("from", "4546") // Eskiz sender nomi
                .addFormDataPart("callback_url", "http://0000.uz/test.php") // Ixtiyoriy
                .build();

        // 3. Requestni yaratamiz
        Request request = new Request.Builder()
                .url("https://notify.eskiz.uz/api/message/sms/send")
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        // 4. So‘rovni yuboramiz
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("SMS yuborildi: " + response.body().string());
            } else {
                System.out.println("Xatolik: " + response.code() + " - " + response.message());
                System.out.println("Javob: " + response.body().string());
            }
        }
    }
}

