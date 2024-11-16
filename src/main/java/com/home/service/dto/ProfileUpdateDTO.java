package com.home.service.dto;

import lombok.Data;

@Data
public class ProfileUpdateDTO {
    private String name;
    private String bio; // Optional for technicians
}
