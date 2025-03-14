package com.home.service.models.enums;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class SocialMedia {
    private String facebook;
    private String twitter;
    private String instagram;
    private String linkedin;

    // Getters and Setters
}
