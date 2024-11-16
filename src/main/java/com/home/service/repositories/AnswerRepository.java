package com.home.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.home.service.models.Answer;
import com.home.service.models.Question;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByBookingIdAndCustomerId(Long bookingId, Long customerId);

    List<Answer> findByQuestion(Question question);
}
