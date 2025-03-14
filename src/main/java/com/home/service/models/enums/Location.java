package com.home.service.models.enums;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Location {
    private double latitude;
    private double longitude;

    // Getters and Setters
}
