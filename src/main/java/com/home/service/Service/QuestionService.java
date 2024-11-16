package com.home.service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.home.service.dto.QuestionDTO;
import com.home.service.dto.QuestionOptionDTO;
import com.home.service.models.Question;
import com.home.service.models.QuestionOption;
import com.home.service.models.QuestionRequest;
import com.home.service.models.enums.QuestionType;
import com.home.service.repositories.QuestionRepository;
import com.home.service.repositories.ServiceRepository;
import com.home.service.models.Services;

import jakarta.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Transactional
    public String createQuestion(QuestionRequest request) {
        Question question = new Question();
        question.setText(request.getText());
        question.setType(request.getType());

        if (request.getType() == QuestionType.MULTIPLE_CHOICE) {
            List<QuestionOption> options = new ArrayList<>();
            for (String optionText : request.getOptions()) {
                QuestionOption option = new QuestionOption();
                option.setQuestion(question);
                option.setOptionText(optionText);
                options.add(option);
            }
            question.setOptions(options);
        }

        Set<Services> services = request.getServiceIds().stream()
                .map(serviceId -> serviceRepository.findById(serviceId)
                        .orElseThrow(() -> new EntityNotFoundException("Service not found with ID: " + serviceId)))
                .collect(Collectors.toSet());

        question.setServices(services);

        questionRepository.save(question);

        return "Question created with ID: " + question.getId();
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public List<QuestionDTO> getQuestionsByServiceId(Long serviceId) {
        Services service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"));

        return questionRepository.findByServices(service)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private QuestionDTO toDTO(Question question) {
        List<QuestionOptionDTO> optionDTOs = question.getOptions().stream()
                .map(option -> new QuestionOptionDTO(option.getId(), option.getOptionText()))
                .collect(Collectors.toList());

        return new QuestionDTO(question.getId(), question.getText(), question.getType(), optionDTOs);
    }
}
