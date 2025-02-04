package com.home.service.models;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Answer extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Customer customer;

    @Column(columnDefinition = "TEXT")
    private String response; // Store the customer's response

    // Getters and setters
}
