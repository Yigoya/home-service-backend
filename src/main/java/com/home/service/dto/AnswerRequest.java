package com.home.service.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswerRequest {
    @NotNull(message = "Booking ID cannot be null")
    private Long bookingId;

    @NotNull(message = "Customer ID cannot be null")
    private Long customerId;

    @NotEmpty(message = "Answers cannot be empty")
    private List<QuestionAnswer> answers;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionAnswer {
        @NotNull(message = "Question ID cannot be null")
        private Long questionId;

        @NotEmpty(message = "Response cannot be empty")
        private String response;
    }
}
