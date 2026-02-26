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
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

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
        this.client = createHttpClient(properties.isDisableSslVerification());
        this.objectMapper = objectMapper;
        this.signatureUtils = signatureUtils;
    }

    private OkHttpClient createHttpClient(boolean disableSslVerification) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .writeTimeout(50, TimeUnit.SECONDS);

        if (!disableSslVerification) {
            return builder.build();
        }

        try {
            final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            X509TrustManager trustManager = (X509TrustManager) trustAllCerts[0];
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            System.out.println("WARNING: Telebirr SSL verification is DISABLED. Use development only.");
            return builder.sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                    .hostnameVerifier(allHostsValid)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize insecure SSL client", e);
        }
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
        String safeMerchantOrderId = merchantOrderId == null ? "" : merchantOrderId.replaceAll("[^A-Za-z0-9]", "");
        if (safeMerchantOrderId.isBlank()) {
            throw new IllegalStateException("Invalid merchantOrderId for Telebirr. It must match ^[A-Za-z0-9]+$.");
        }

        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("notify_url", properties.getNotifyUrl());
        bizContent.put("appid", properties.getMerchantAppId());
        bizContent.put("merch_code", properties.getMerchantCode());
        bizContent.put("merch_order_id", safeMerchantOrderId);
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

        String normalizedToken = normalizeFabricToken(fabricToken);
        String configuredSignType = properties.getSignType();
        String fallbackSignType = "SHA256withRSAandMGF1";

        String[] signTypesToTry = configuredSignType.equalsIgnoreCase(fallbackSignType)
                ? new String[] { configuredSignType }
                : new String[] { configuredSignType, fallbackSignType };

        String[] authHeadersToTry = new String[] { normalizedToken, "Bearer " + normalizedToken };

        for (String signType : signTypesToTry) {
            String payload = buildPreOrderPayload(bizContent, signType);
            for (String authHeader : authHeadersToTry) {
                PreOrderResult attempt = callPreOrderWithAuthHeader(payload, authHeader, signType);
                if (attempt != null) {
                    return attempt;
                }
            }
        }

        throw new IllegalStateException(
                "Telebirr preOrder failed after trying token formats and sign types. See logs above.");
    }

    private String normalizeFabricToken(String fabricToken) {
        if (fabricToken == null) {
            return "";
        }
        String trimmed = fabricToken.trim();
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return trimmed.substring(7).trim();
        }
        return trimmed;
    }

    private String buildPreOrderPayload(Map<String, Object> bizContent, String signType) {
        Map<String, Object> request = new HashMap<>();
        request.put("timestamp", String.valueOf(Instant.now().getEpochSecond()));
        request.put("nonce_str", UUID.randomUUID().toString().replace("-", ""));
        request.put("method", "payment.preorder");
        request.put("version", "1.0");
        request.put("biz_content", bizContent);

        String sign = signatureUtils.sign(request, properties.getPrivateKeyPath(), signType);
        request.put("sign", sign);
        request.put("sign_type", signType);

        try {
            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize preOrder payload", e);
        }
    }

    private PreOrderResult callPreOrderWithAuthHeader(String payload, String authorizationHeaderValue, String signType) {
        Request httpRequest = new Request.Builder()
                .url(properties.getBaseUrl() + "/payment/v1/merchant/preOrder")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-APP-Key", properties.getFabricAppId())
                .addHeader("Authorization", authorizationHeaderValue)
                .post(RequestBody.create(payload, JSON))
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "{}";
            if (!response.isSuccessful()) {
                System.out.println("Telebirr preOrder failed: status=" + response.code()
                        + ", authHeaderPrefix="
                        + (authorizationHeaderValue.startsWith("Bearer ") ? "Bearer" : "RawToken")
                    + ", signType=" + signType
                        + ", response=" + responseBody);
                return null;
            }
            Map<?, ?> result = objectMapper.readValue(responseBody, Map.class);
            Map<?, ?> biz = (Map<?, ?>) result.get("biz_content");
            String prepayId = biz != null && biz.get("prepay_id") != null ? biz.get("prepay_id").toString() : null;
            if (prepayId == null) {
                throw new IllegalStateException("Telebirr prepay_id missing in response: " + responseBody);
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