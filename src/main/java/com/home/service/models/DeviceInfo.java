package com.home.service.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfo extends BaseEntity {

    @NotBlank(message = "FCM token is required")
    private String FCMToken;

    @NotBlank(message = "Device type is required")
    private String deviceType; // e.g., "Android", "iOS"

    @NotBlank(message = "Device model is required")
    private String deviceModel; // e.g., "Pixel 6", "iPhone 13"

    private String operatingSystem; // e.g., "Android 12", "iOS 16"

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Getters and setters
}
