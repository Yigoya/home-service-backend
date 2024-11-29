package com.home.service.models;

import java.util.List;

import com.home.service.models.enums.QuestionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionRequest {
    @NotEmpty(message = "Question text cannot be empty")
    private String text;

    @NotNull(message = "Question type cannot be null")
    private QuestionType type;

    private List<String> options; // Only used for multiple-choice questions

    @NotEmpty(message = "Service IDs cannot be empty")
    private List<Long> serviceIds;
    // Getters and setters
}
