package com.home.service.dto.admin;

import java.util.List;

import com.home.service.dto.ServiceDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDetailDTO {
    private Long customerId;
    private String name;
    private String email;
    private String phoneNumber;
    private String profileImage;
    private int bookings;
    private List<ServiceDTO> services;
    private AddressDTO address;
}
