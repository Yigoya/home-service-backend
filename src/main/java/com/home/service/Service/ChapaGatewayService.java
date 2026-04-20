package com.home.service.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.service.config.ChapaProperties;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class ChapaGatewayService {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String BASE_URL = "https://api.chapa.co/v1";

    private final ChapaProperties properties;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client;

    public ChapaGatewayService(ChapaProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(40, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS)
                .writeTimeout(40, TimeUnit.SECONDS)
                .build();
    }

    public InitializeResult initialize(String txRef, BigDecimal amount, String firstName, String lastName, String email,
            String title) {
        validateSecretKey();

        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount.toPlainString());
        body.put("currency", properties.getCurrency());
        body.put("email", email);
        body.put("first_name", firstName);
        body.put("last_name", lastName);
        body.put("tx_ref", txRef != null ? txRef : UUID.randomUUID().toString());
        body.put("callback_url", properties.getCallbackUrl());
        body.put("return_url", properties.getReturnUrl());

        Map<String, String> customization = new HashMap<>();
        customization.put("title", properties.getCustomizationTitle() != null ? properties.getCustomizationTitle() : "Subscription");
        customization.put("description",
                properties.getCustomizationDescription() != null ? properties.getCustomizationDescription() : "Subscription payment");
        if (properties.getCustomizationLogo() != null && !properties.getCustomizationLogo().isBlank()) {
            customization.put("logo", properties.getCustomizationLogo());
        }
        body.put("customizations", customization);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("purpose", "subscription");
        metadata.put("plan_title", title);
        body.put("meta", metadata);

        String payload;
        try {
            payload = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize Chapa initialize payload", e);
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "/transaction/initialize")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + properties.getSecretKey())
                .post(RequestBody.create(payload, JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "{}";
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Chapa initialize failed: " + response.code() + " - " + responseBody);
            }
            Map<?, ?> result = objectMapper.readValue(responseBody, Map.class);
            Map<?, ?> data = (Map<?, ?>) result.get("data");
            if (data == null || data.get("checkout_url") == null) {
                throw new IllegalStateException("Chapa checkout_url missing in response: " + responseBody);
            }
            return new InitializeResult(data.get("checkout_url").toString(), payload, responseBody);
        } catch (IOException e) {
            throw new IllegalStateException("Chapa initialize request failed", e);
        }
    }

    public VerifyResult verify(String txRef) {
        validateSecretKey();
        if (txRef == null || txRef.isBlank()) {
            throw new IllegalArgumentException("txRef is required for Chapa verification");
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "/transaction/verify/" + txRef)
                .addHeader("Authorization", "Bearer " + properties.getSecretKey())
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "{}";
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Chapa verify failed: " + response.code() + " - " + responseBody);
            }
            Map<?, ?> result = objectMapper.readValue(responseBody, Map.class);
            String status = result.get("status") != null ? result.get("status").toString() : null;
            Map<?, ?> data = (Map<?, ?>) result.get("data");
            String txStatus = data != null && data.get("status") != null ? data.get("status").toString() : null;
            return new VerifyResult(status, txStatus, responseBody);
        } catch (IOException e) {
            throw new IllegalStateException("Chapa verify request failed", e);
        }
    }

    private void validateSecretKey() {
        if (properties.getSecretKey() == null || properties.getSecretKey().isBlank()) {
            throw new IllegalStateException("Chapa secret key is not configured");
        }
    }

    public record InitializeResult(String checkoutUrl, String rawRequest, String rawResponse) {
    }

    public record VerifyResult(String status, String txStatus, String rawResponse) {
    }
}
