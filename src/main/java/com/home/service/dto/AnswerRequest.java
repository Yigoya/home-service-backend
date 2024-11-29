package com.home.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class AnswerRequest {

    @NotNull(message = "Booking ID is required.")
    private Long bookingId;

    @NotNull(message = "Customer ID is required.")
    private Long customerId;

    @NotEmpty(message = "Answers list cannot be empty.")
    private List<@Valid AnswerDTO> answers;

    // Getters and setters
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<AnswerDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerDTO> answers) {
        this.answers = answers;
    }

    public static class AnswerDTO {

        @NotNull(message = "Question ID is required.")
        private Long questionId;

        @NotEmpty(message = "Response cannot be empty.")
        private String response;

        // Getters and setters
        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }
}
