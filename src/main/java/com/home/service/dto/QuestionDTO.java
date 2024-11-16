package com.home.service.dto;

import java.util.List;

import com.home.service.models.enums.QuestionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private Long id;
    private String text;
    private QuestionType type;
    private List<QuestionOptionDTO> options;

    // Constructor, getters, setters
}
