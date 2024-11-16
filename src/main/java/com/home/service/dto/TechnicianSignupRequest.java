package com.home.service.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianSignupRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    // Technician-specific fields
    @NotBlank(message = "Bio is required")
    private String bio;

    private MultipartFile profileImage;

    private MultipartFile idCardImage;

    private List<MultipartFile> documents;

    // TechnicianAddress fields
    private String street;
    private String city;
    private String subcity;
    private String wereda;
    private String country;
    private String zipCode;
    private Double latitude;
    private Double longitude;

    // List of service IDs to associate with this technician
    @NotEmpty(message = "Service IDs must not be empty")
    private List<Long> serviceIds;
}
