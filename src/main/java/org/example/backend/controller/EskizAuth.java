package org.example.backend.controller;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class EskizAuth {
    public static void main(String[] args) throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("email", "educationbakayev@gmail.com")
                .addFormDataPart("password", "nL96kqL2njbBrhpvC4Xj8oIxIYjDytfOxwwbf688")
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

                System.out.println("Token: " + token);
            } else {
                System.out.println("Xatolik: " + response.code() + " - " + response.message());
            }
        }
    }
}
