package com.home.service.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class OperatorSignupRequest {

    // User-related fields
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    // Operator-specific fields
    private String assignedRegion;
    private MultipartFile idCardImage;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAssignedRegion() {
        return assignedRegion;
    }

    public void setAssignedRegion(String assignedRegion) {
        this.assignedRegion = assignedRegion;
    }

    public MultipartFile getIdCardImage() {
        return idCardImage;
    }

    public void setIdCardImage(MultipartFile idCardImage) {
        this.idCardImage = idCardImage;
    }
}
