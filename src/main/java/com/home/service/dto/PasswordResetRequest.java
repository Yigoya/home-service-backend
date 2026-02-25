package com.home.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public class PasswordResetRequest {
    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,13}$", message = "Invalid phone number format")
    private String phoneNumber;

    // Getter and setter
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
}
