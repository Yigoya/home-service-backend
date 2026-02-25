package com.home.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,13}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "FCM token is required")
    @JsonProperty("FCMToken")
    private String FCMToken;

    @NotBlank(message = "Device type is required")
    private String deviceType;

    @NotBlank(message = "Device model is required")
    private String deviceModel;

    @NotBlank(message = "Operating system is required")
    private String operatingSystem;
}
