package com.home.service.dto;

import java.util.List;

import com.home.service.models.enums.QuestionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionWithAnswerDTO {
    private Long questionId;
    private String text;
    private String type;
    private List<QuestionOptionDTO> options;
    private List<AnswerDTO> answers;

    // Constructor, getters, setters
}
