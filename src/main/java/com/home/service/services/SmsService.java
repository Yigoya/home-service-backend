package com.home.service.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SmsService {

    private final RestTemplate restTemplate;

    // @Value("${sms.api.key}")
    private String apiKey = "\teyJhbGciOiJIUzI1NiJ9.eyJpZGVudGlmaWVyIjoiU3VpOGtaZmxvS1pqQ1Zzb2g1MTVsRTBKVGtIMHBZTVoiLCJleHAiOjE4OTY2MTYwODgsImlhdCI6MTczODg0OTY4OCwianRpIjoiMDBiZDJlMGItOWU3Mi00OTQ0LWI2M2YtNDc5NGVhODU3N2RkIn0.mZoYExLrhwI9KGSFo4TEt4LKVOdjVBa-wvwYXhJr_8E";

    public SmsService() {
        this.restTemplate = new RestTemplate();
    }

    public String sendSms(String to, String message) {
        String url = "https://api.afromessage.com/api/send";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey); // Assuming apiKey is the token

        Map<String, String> body = new HashMap<>();
        body.put("from", "e80ad9d8-adf3-463f-80f4-7c4b39f7f164"); // Replace with actual identifier
        body.put("sender", "Hulu Moya"); // Replace with actual sender name
        body.put("to", to);
        body.put("message", message);
        body.put("callback", "https://home-service-managment.vercel.app/"); // Replace with actual callback URL

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody();
    }
}
