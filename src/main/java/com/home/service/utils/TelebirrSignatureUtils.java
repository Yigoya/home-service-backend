package com.home.service.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Component
public class TelebirrSignatureUtils {

    private static final Set<String> EXCLUDED_FIELDS = new HashSet<>(Set.of(
            "sign", "sign_type", "header", "refund_info", "openType", "raw_request", "biz_content",
            "wallet_reference_data"));

    private final ObjectMapper objectMapper;

    public TelebirrSignatureUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public String sign(Map<String, ?> data, String privateKeyPath, String signType) {
        try {
            String content = canonicalize(data);
            Signature signature = buildSignature(signType);
            signature.initSign(loadPrivateKey(privateKeyPath));
            configureSignatureParams(signature, signType);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign request: " + e.getMessage(), e);
        }
    }

    public String sign(Map<String, ?> data, String privateKeyPath) {
        return sign(data, privateKeyPath, "SHA256WithRSA");
    }

    public boolean verify(Map<String, ?> data, String signatureBase64, String publicKeyPath, String signType) {
        try {
            String content = canonicalize(data);
            Signature signature = buildSignature(signType);
            signature.initVerify(loadPublicKey(publicKeyPath));
            configureSignatureParams(signature, signType);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(signatureBase64));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean verify(Map<String, ?> data, String signatureBase64, String publicKeyPath) {
        return verify(data, signatureBase64, publicKeyPath, "SHA256WithRSA");
    }

    private String canonicalize(Map<String, ?> data) {
        Map<String, Object> sorted = new TreeMap<>();
        for (Map.Entry<String, ?> entry : data.entrySet()) {
            String key = entry.getKey();
            if (EXCLUDED_FIELDS.contains(key)) {
                continue;
            }
            if (entry.getValue() == null) {
                continue;
            }

            if ("biz_content".equals(key)) {
                flattenBizContent(entry.getValue(), sorted);
                continue;
            }

            sorted.put(key, entry.getValue());
        }
        return sorted.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + normalizeValue(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    @SuppressWarnings("unchecked")
    private void flattenBizContent(Object value, Map<String, Object> target) {
        if (value == null) {
            return;
        }

        Map<String, Object> bizMap = null;
        if (value instanceof Map<?, ?> rawMap) {
            bizMap = (Map<String, Object>) rawMap;
        } else if (value instanceof String str && str.trim().startsWith("{")) {
            try {
                bizMap = objectMapper.readValue(str, Map.class);
            } catch (Exception ignored) {
                return;
            }
        }

        if (bizMap == null) {
            return;
        }

        for (Map.Entry<String, Object> bizEntry : bizMap.entrySet()) {
            if (EXCLUDED_FIELDS.contains(bizEntry.getKey())) {
                continue;
            }
            if (bizEntry.getValue() == null) {
                continue;
            }
            target.put(bizEntry.getKey(), bizEntry.getValue());
        }
    }

    private String normalizeValue(Object value) {
        if (value instanceof Map<?, ?> mapValue) {
            return toJson(mapValue);
        }
        return String.valueOf(value);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize value", e);
        }
    }

    private Signature buildSignature(String signType) throws Exception {
        if (isRsaPss(signType)) {
            return Signature.getInstance("RSASSA-PSS");
        }
        return Signature.getInstance("SHA256withRSA");
    }

    private void configureSignatureParams(Signature signature, String signType) throws Exception {
        if (isRsaPss(signType)) {
            PSSParameterSpec pssSpec = new PSSParameterSpec(
                    "SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA256,
                    32,
                    1);
            signature.setParameter(pssSpec);
        }
    }

    private boolean isRsaPss(String signType) {
        if (signType == null || signType.isBlank()) {
            return false;
        }
        String normalized = signType.trim().toUpperCase();
        return normalized.contains("MGF1") || normalized.contains("PSS");
    }

    private PrivateKey loadPrivateKey(String keyPath) throws Exception {
        byte[] keyBytes = readKeyBytes(keyPath);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey loadPublicKey(String keyPath) throws Exception {
        byte[] keyBytes = readKeyBytes(keyPath);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private byte[] readKeyBytes(String keyPath) throws IOException {
        String pem = readKeyString(keyPath);
        String cleaned = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(cleaned);
    }

    private String readKeyString(String keyPath) throws IOException {
        if (keyPath == null || keyPath.isBlank()) {
            throw new IllegalStateException("Telebirr key path is not configured");
        }
        if (keyPath.startsWith("classpath:")) {
            String classpathLocation = keyPath.replace("classpath:", "");
            return new String(new ClassPathResource(classpathLocation).getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
        }
        return Files.readString(Path.of(keyPath));
    }
}