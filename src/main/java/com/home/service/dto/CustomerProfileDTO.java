package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import com.home.service.models.Customer;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private List<String> serviceHistory;
    private String profileImage;

    public CustomerProfileDTO(Customer customer) {

        this.id = customer.getId();

        this.name = customer.getUser().getName();

        this.email = customer.getUser().getEmail();

        this.phoneNumber = customer.getUser().getPhoneNumber();

        this.profileImage = customer.getUser().getProfileImage();

        // initialize other fields

    }
}
