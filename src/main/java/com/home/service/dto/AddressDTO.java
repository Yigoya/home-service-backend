package com.home.service.dto;

import com.home.service.models.Address;
import com.home.service.models.TechnicianAddress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    private Long id;
    private Long customerId;
    private String street;
    private String city;
    private String subcity;
    private String wereda;
    private String state;
    private String country;
    private String zipCode;
    private Double latitude;
    private Double longitude;

    public AddressDTO(Address address) {

        // initialize fields from address object

        this.street = address.getStreet();

        this.city = address.getCity();

        this.subcity = address.getSubcity();

        this.wereda = address.getWereda();

        this.state = address.getState();

        this.country = address.getCountry();

        this.zipCode = address.getZipCode();

        this.latitude = address.getLatitude();

        this.longitude = address.getLongitude();

    }

    public AddressDTO(TechnicianAddress address) {

        // initialize fields from address object

        this.street = address.getStreet();

        this.city = address.getCity();

        this.subcity = address.getSubcity();

        this.wereda = address.getWereda();

        this.country = address.getCountry();

        this.zipCode = address.getZipCode();

        this.latitude = address.getLatitude();

        this.longitude = address.getLongitude();

    }
}
