package com.home.service.models;

import java.util.List;

import com.home.service.models.enums.QuestionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionRequest {
    private String text;
    private QuestionType type;
    private List<String> options; // Only used for multiple-choice questions
    private List<Long> serviceIds;
    // Getters and setters
}
