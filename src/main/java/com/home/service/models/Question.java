package com.home.service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.home.service.models.enums.QuestionType;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question extends BaseEntity {

    @Column(nullable = false)
    private String text;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<QuestionOption> options;

    @ManyToMany
    @JoinTable(name = "service_questions", joinColumns = @JoinColumn(name = "question_id"), inverseJoinColumns = @JoinColumn(name = "service_id"))
    private Set<Services> services;

    public void removeService(Services service) {
        services.remove(service);
        service.getQuestions().remove(this);
    }

    // Getters and setters
}
