package com.home.service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import com.home.service.models.enums.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String profileImage;

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.INACTIVE;

    @Enumerated(EnumType.STRING)
    private EthiopianLanguage preferredLanguage = EthiopianLanguage.ENGLISH;

    private String authProvider; // e.g., "google", "facebook", "direct"
    private String providerId; // e.g., Google UID, Facebook UID

}
