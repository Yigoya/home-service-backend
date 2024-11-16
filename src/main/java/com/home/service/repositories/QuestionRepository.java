package com.home.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.home.service.models.Services;

import com.home.service.models.Question;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByServices(Services service);
}
