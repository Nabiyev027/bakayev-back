package org.example.backend.controller;

import okhttp3.*;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.Properties;

public class EskizAuth {
    private static final String PROPERTIES_PATH = "src/main/resources/eskiz.properties";
    private static final String TOKEN_FILE_PATH = "src/main/resources/token.txt";
    private static final int TOKEN_VALID_DAYS = 30;

    public static void main(String[] args) throws IOException {
        String token = getToken();
        System.out.println("Foydalanilayotgan token: " + token);
    }

    // Tokenni olish va yangilash
    private static String getToken() throws IOException {
        if (Files.exists(Paths.get(TOKEN_FILE_PATH))) {
            String[] lines = Files.readAllLines(Paths.get(TOKEN_FILE_PATH)).toArray(new String[0]);

            if (lines.length == 2) {
                String savedToken = lines[0];
                LocalDateTime savedDate = LocalDateTime.parse(lines[1]);

                if (Duration.between(savedDate, LocalDateTime.now()).toDays() < TOKEN_VALID_DAYS) {
                    return savedToken;
                }
            }
        }

        // Token muddati tugagan bo‘lsa, yangisini olish
        return getNewTokenAndSave();
    }

    // Eskiz API orqali yangi token olish va faylga yozish
    private static String getNewTokenAndSave() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(PROPERTIES_PATH));

        String email = properties.getProperty("eskiz.email");
        String password = properties.getProperty("eskiz.password");

        OkHttpClient client = new OkHttpClient();

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("email", email)
                .addFormDataPart("password", password)
                .build();

        Request request = new Request.Builder()
                .url("https://notify.eskiz.uz/api/auth/login")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);
                String token = json.getJSONObject("data").getString("token");

                // Faylga yozish: token va vaqt
                try (FileWriter writer = new FileWriter(TOKEN_FILE_PATH)) {
                    writer.write(token + "\n" + LocalDateTime.now().toString());
                }

                return token;
            } else {
                throw new IOException("Token olishda xatolik: " + response.code() + " - " + response.message());
            }
        }
    }
}

