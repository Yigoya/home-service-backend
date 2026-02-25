package com.home.service.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.service.config.TelebirrProperties;
import com.home.service.utils.TelebirrSignatureUtils;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class TelebirrService {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final TelebirrProperties properties;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final TelebirrSignatureUtils signatureUtils;

    public TelebirrService(TelebirrProperties properties, ObjectMapper objectMapper,
            TelebirrSignatureUtils signatureUtils) {
        this.properties = properties;
        this.client = new OkHttpClient.Builder()
            .connectTimeout(50, TimeUnit.SECONDS)
            .readTimeout(50, TimeUnit.SECONDS)
            .writeTimeout(50, TimeUnit.SECONDS)
            .build();
        this.objectMapper = objectMapper;
        this.signatureUtils = signatureUtils;
    }

    public String applyFabricToken() {
        System.out.println("Requesting Telebirr token with appSecret: " + properties.getAppSecret() + " and fabricAppId: " + properties.getFabricAppId());
        Map<String, Object> body = Map.of("appSecret", properties.getAppSecret());
        String payload;
        try {
            payload = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize token payload", e);
        }

        Request request = new Request.Builder()
                .url(properties.getBaseUrl() + "/payment/v1/token")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-APP-Key", properties.getFabricAppId())
                .post(RequestBody.create(payload, JSON))
                .build();
        System.out.println("Requesting Telebirr token with payload: " + request);
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "{}";
            if (!response.isSuccessful()) {
                throw new IllegalStateException(
                        "Telebirr token request failed: " + response.code() + " - " + responseBody);
            }
            Map<?, ?> result = objectMapper.readValue(responseBody, Map.class);
            Object token = result.get("token");
            if (token == null) {
                throw new IllegalStateException("Telebirr token missing in response");
            }
            return token.toString();
        } catch (IOException e) {
            System.out.println("Error requesting Telebirr token: " + e.getMessage() + " - " + e.getCause() + " - "
                    + e.getStackTrace() + " - " + e.toString());
            throw new IllegalStateException("Telebirr token request faileds", e);
        }
    }

    public PreOrderResult createPreOrder(String fabricToken, String title, BigDecimal amount,
            String merchantOrderId) {
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("notify_url", properties.getNotifyUrl());
        bizContent.put("appid", properties.getMerchantAppId());
        bizContent.put("merch_code", properties.getMerchantCode());
        bizContent.put("merch_order_id", merchantOrderId);
        bizContent.put("trade_type", "Checkout");
        bizContent.put("title", title);
        bizContent.put("total_amount", amount.toPlainString());
        bizContent.put("trans_currency", "ETB");
        bizContent.put("timeout_express", "120m");
        bizContent.put("business_type", "BuyGoods");
        bizContent.put("payee_identifier", properties.getMerchantCode());
        bizContent.put("payee_identifier_type", "04");
        bizContent.put("payee_type", "5000");
        bizContent.put("redirect_url", properties.getRedirectUrl());
        bizContent.put("callback_info", "subscription");

        Map<String, Object> request = new HashMap<>();
        request.put("timestamp", String.valueOf(Instant.now().getEpochSecond()));
        request.put("nonce_str", UUID.randomUUID().toString().replace("-", ""));
        request.put("method", "payment.preorder");
        request.put("version", "1.0");
        request.put("biz_content", bizContent);

        String sign = signatureUtils.sign(request, properties.getPrivateKeyPath(), properties.getSignType());
        request.put("sign", sign);
        request.put("sign_type", properties.getSignType());

        String payload;
        try {
            payload = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize preOrder payload", e);
        }

        Request httpRequest = new Request.Builder()
                .url(properties.getBaseUrl() + "/payment/v1/merchant/preOrder")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-APP-Key", properties.getFabricAppId())
                .addHeader("Authorization", fabricToken)
                .post(RequestBody.create(payload, JSON))
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Telebirr preOrder failed: " + response.code());
            }
            String responseBody = response.body() != null ? response.body().string() : "{}";
            Map<?, ?> result = objectMapper.readValue(responseBody, Map.class);
            Map<?, ?> biz = (Map<?, ?>) result.get("biz_content");
            String prepayId = biz != null && biz.get("prepay_id") != null ? biz.get("prepay_id").toString() : null;
            if (prepayId == null) {
                throw new IllegalStateException("Telebirr prepay_id missing in response");
            }
            return new PreOrderResult(prepayId, payload, responseBody);
        } catch (IOException e) {
            throw new IllegalStateException("Telebirr preOrder failed", e);
        }
    }

    public String buildCheckoutUrl(String prepayId) {
        Map<String, String> map = new HashMap<>();
        map.put("appid", properties.getMerchantAppId());
        map.put("merch_code", properties.getMerchantCode());
        map.put("nonce_str", UUID.randomUUID().toString().replace("-", ""));
        map.put("prepay_id", prepayId);
        map.put("timestamp", String.valueOf(Instant.now().getEpochSecond()));

        String sign = signatureUtils.sign(map, properties.getPrivateKeyPath(), properties.getSignType());

        String rawRequest = String.join("&",
                "appid=" + map.get("appid"),
                "merch_code=" + map.get("merch_code"),
                "nonce_str=" + map.get("nonce_str"),
                "prepay_id=" + map.get("prepay_id"),
                "timestamp=" + map.get("timestamp"),
                "sign=" + sign,
                "sign_type=" + properties.getSignType());

        return properties.getWebBaseUrl() + rawRequest + "&version=1.0&trade_type=Checkout";
    }

    public record PreOrderResult(String prepayId, String rawRequest, String rawResponse) {
    }
}