package com.home.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private Long userId;

    @NotBlank(message = "Title is mandatory")
    private String title;

    @NotBlank(message = "Body is mandatory")
    private String body;

    private String imageUrl;
    private String targetPage;
}
