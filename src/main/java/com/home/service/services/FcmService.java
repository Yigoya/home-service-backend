package com.home.service.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.home.service.models.DeviceInfo;
import com.home.service.models.User;

import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

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

  public void sendNotification(User user, String title, String body, String imageUrl) {
    // Initialize HTTP client
    OkHttpClient client = new OkHttpClient();

    // Retrieve all device tokens for the user
    List<DeviceInfo> devices = user.getDevices();

    if (devices.isEmpty()) {
      System.out.println("No devices found for user: " + user.getEmail());
      return;
    }

    // Loop through each device and send notification
    for (DeviceInfo device : devices) {
      String FCMToken = device.getFCMToken();

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
          FCMToken,
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
          System.err.println("Failed to send notification to token: " + FCMToken);
          System.err.println("Response: " + response.body().string());
        } else {
          System.out.println("Notification sent successfully to token: " + FCMToken);
        }
      } catch (IOException e) {
        System.err.println("Error sending notification to token: " + FCMToken);
        e.printStackTrace();
      }
    }
  }

}
