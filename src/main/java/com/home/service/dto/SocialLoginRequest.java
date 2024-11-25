package com.home.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocialLoginRequest {
    @NotBlank(message = "ID token is required")
    private String idToken;

    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    private String phoneNumber;

    @NotBlank(message = "Auth provider is required")
    private String provider; // e.g., "google", "facebook", "direct"
}
