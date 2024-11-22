package com.home.service.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.home.service.dto.AnswerDTO;
import com.home.service.models.Answer;
import com.home.service.repositories.AnswerRepository;

import jakarta.transaction.Transactional;

@Service
public class AnswerService {
    @Autowired
    private AnswerRepository answerRepository;

    @Transactional
    public List<AnswerDTO> getNecessaryAnswerData(Long bookingId, Long customerId) {
        List<Answer> answers = answerRepository.findByBookingIdAndCustomerId(bookingId, customerId);
        return answers.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private AnswerDTO convertToDto(Answer answer) {
        AnswerDTO dto = new AnswerDTO();
        dto.setAnswerId(answer.getId());
        dto.setResponse(answer.getResponse());
        return dto;
    }
}
