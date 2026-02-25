package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    @NotBlank(message = "Name is mandatory")
    private String name;

    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone number is mandatory")
    @Size(min = 10, max = 15, message = "Phone number should be between 10 and 15 characters")
    private String phoneNumber;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, message = "Password should have at least 6 characters")
    private String password;
}
