package com.home.service.services;

import com.google.auth.oauth2.GoogleCredentials;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;

@Service
public class FcmService {

    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/home-service-92c52/messages:send";
    private String accessToken;

    public FcmService() throws IOException {
        // Load service account credentials
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new FileInputStream("./serviceAccountKey.json"))
                .createScoped("https://www.googleapis.com/auth/firebase.messaging");

        // Fetch the access token
        googleCredentials.refreshIfExpired();
        this.accessToken = googleCredentials.getAccessToken().getTokenValue();
    }

    public void sendNotification(String targetToken, String title, String body, String imageUrl) {
        OkHttpClient client = new OkHttpClient();

        // Construct JSON payload with optional image
        String jsonPayload = """
                {
                  "message": {
                    "token": "%s",
                    "notification": {
                      "title": "%s",
                      "body": "%s" %s
                    }
                  }
                }
                """.formatted(
                targetToken,
                title,
                body,
                imageUrl != null && !imageUrl.isEmpty()
                        ? ", \"image\": \"" + imageUrl + "\"" // Include image if provided
                        : "" // Exclude image if not provided
        );

        RequestBody requestBody = RequestBody.create(
                jsonPayload,
                MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(FCM_URL)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            System.out.println("Notification sent successfully: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
