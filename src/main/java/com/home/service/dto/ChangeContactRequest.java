package com.home.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeContactRequest {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @Email(message = "Invalid new email format")
    private String newEmail;

    @Pattern(regexp = "\\+?[0-9]{10,15}", message = "Invalid phone number format")
    private String newPhoneNumber;
}
