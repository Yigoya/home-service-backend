package com.home.service.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "search_logs")
@Getter
@Setter
public class SearchLog extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String query;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Services category;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private BusinessLocation location;

    private int resultCount;

    // Getters and Setters
}