package com.home.service.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Component
public class TelebirrSignatureUtils {

    private final ObjectMapper objectMapper;

    public TelebirrSignatureUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public String sign(Map<String, ?> data, String privateKeyPath) {
        try {
            String content = canonicalize(data);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(loadPrivateKey(privateKeyPath));
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign request: " + e.getMessage(), e);
        }
    }

    public boolean verify(Map<String, ?> data, String signatureBase64, String publicKeyPath) {
        try {
            String content = canonicalize(data);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(loadPublicKey(publicKeyPath));
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(signatureBase64));
        } catch (Exception e) {
            return false;
        }
    }

    private String canonicalize(Map<String, ?> data) {
        Map<String, Object> sorted = new TreeMap<>();
        for (Map.Entry<String, ?> entry : data.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            sorted.put(entry.getKey(), entry.getValue());
        }
        return sorted.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + normalizeValue(entry.getValue()))
                .collect(Collectors.joining("&"));
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