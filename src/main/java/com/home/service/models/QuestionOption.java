package com.home.service.models;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionOption extends BaseEntity {

    @Column(nullable = false)
    private String optionText;

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonBackReference
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Question question;

    // Getters and setters
}
