package com.home.service.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianAddressDTO {
    private String street;
    private String city;
    private String subcity;
    private String wereda;
    private String country;
    private String zipCode;
    private Double latitude;
    private Double longitude;
}
