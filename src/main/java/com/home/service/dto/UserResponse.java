package com.home.service.dto;

import com.home.service.models.enums.EthiopianLanguage;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String role;
    private String status;
    private String profileImage;
    private EthiopianLanguage language;

}
