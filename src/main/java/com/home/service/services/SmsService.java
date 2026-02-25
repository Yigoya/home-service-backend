package com.home.service.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SmsService {

    private final RestTemplate restTemplate;

    @Value("${sms.geez.token:}")
    private String apiToken;

    @Value("${sms.geez.shortcodeId:}")
    private String shortcodeId;

    public SmsService() {
        this.restTemplate = new RestTemplate();
    }

    public String sendSms(String to, String message) {
        if (apiToken == null || apiToken.isBlank()) {
            throw new IllegalStateException("SMS API token is not configured");
        }

        String normalizedPhone = normalizePhone(to);
        String encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8);
        StringBuilder url = new StringBuilder("https://api.geezsms.com/api/v1/sms/send");
        url.append("?token=").append(URLEncoder.encode(apiToken, StandardCharsets.UTF_8));
        url.append("&phone=").append(URLEncoder.encode(normalizedPhone, StandardCharsets.UTF_8));
        url.append("&msg=").append(encodedMsg);
        if (shortcodeId != null && !shortcodeId.isBlank()) {
            url.append("&shortcode_id=").append(URLEncoder.encode(shortcodeId, StandardCharsets.UTF_8));
        }

        return restTemplate.getForObject(url.toString(), String.class);
    }

    private String normalizePhone(String phone) {
        if (phone == null) return "";
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.startsWith("0") && digits.length() == 10) {
            digits = "251" + digits.substring(1);
        } else if (!digits.startsWith("251") && digits.length() == 9) {
            // handle 9XXXXXXXX local format
            digits = "251" + digits;
        }
        return digits;
    }
}
