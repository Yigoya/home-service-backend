package com.home.service.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.home.service.models.DeviceInfo;
import com.home.service.models.User;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Service
public class FcmService {

  private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/home-service-92c52/messages:send";
  private static final String FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
  private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

  private String accessToken;
  private final Logger logger = LoggerFactory.getLogger(FcmService.class);

  @PostConstruct
  public void initialize() throws IOException {
    GoogleCredentials googleCredentials;
    try {
      googleCredentials = GoogleCredentials
          .fromStream(new FileInputStream("/root/home-service-backend/serviceAccountKey.json"))
          .createScoped(FCM_SCOPE);

    } catch (IOException e) {
      googleCredentials = GoogleCredentials
          .fromStream(new FileInputStream("./serviceAccountKey.json"))

          .createScoped(FCM_SCOPE);
    }

    // Fetch and set the access token
    googleCredentials.refreshIfExpired();
    this.accessToken = googleCredentials.getAccessToken().getTokenValue();
  }

  public void sendNotification(User user, String title, String body, String imageUrl, String targetPage, String value) {
    OkHttpClient client = new OkHttpClient();
    List<DeviceInfo> devices = user.getDevices();

    if (devices.isEmpty()) {
      logger.warn("No devices found for user: {}", user.getEmail());
      return;
    }

    for (DeviceInfo device : devices) {
      String fcmToken = device.getFCMToken();
      if (fcmToken == null || fcmToken.isEmpty()) {
        logger.warn("Invalid FCM token for device of user: {}", user.getEmail());
        continue;
      }

      String jsonPayload = constructPayload(fcmToken, title, body, imageUrl, targetPage, value);

      RequestBody requestBody = RequestBody.create(jsonPayload, JSON_MEDIA_TYPE);
      Request request = new Request.Builder()
          .url(FCM_URL)
          .addHeader("Authorization", "Bearer " + accessToken)
          .addHeader("Content-Type", "application/json")
          .post(requestBody)
          .build();

      try (Response response = client.newCall(request).execute()) {
        if (!response.isSuccessful()) {
          logger.error("Failed to send notification to token: {}. Response: {}", fcmToken, response.body().string());
        } else {
          logger.info("Notification sent successfully to token: {}", fcmToken);
        }
      } catch (IOException e) {
        logger.error("Error sending notification to token: {}", fcmToken, e);
      }
    }
  }

  private String constructPayload(String fcmToken, String title, String body, String imageUrl, String targetPage,
      String value) {
    String imageJsonPart = (imageUrl != null && !imageUrl.isEmpty())
        ? String.format(", \"image\": \"%s\"", imageUrl)
        : "";

    String dataJsonPart = targetPage != null && value != null
        ? String.format(", \"data\": {\"targetPage\": \"%s\", \"value\": \"%s\"}", targetPage, value)
        : targetPage != null
            ? String.format(", \"data\": {\"targetPage\": \"%s\"}", targetPage)
            : value != null
                ? String.format(", \"data\": {\"value\": \"%s\"}", value)
                : "";

    return """
        {
          "message": {
            "token": "%s",
            "notification": {
              "title": "%s",
              "body": "%s" %s
            }%s
          }
        }
        """.formatted(fcmToken, title, body, imageJsonPart, dataJsonPart);
  }

}
