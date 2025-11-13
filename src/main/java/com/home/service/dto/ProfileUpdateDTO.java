package com.home.service.dto;

import lombok.Data;

@Data
public class ProfileUpdateDTO {
    private String name;
    private String bio; // Optional for technicians
    // Optional social links to update
    private String website;
    private String facebook;
    private String twitter;
    private String instagram;
    private String linkedin;
    private String whatsapp;
}
