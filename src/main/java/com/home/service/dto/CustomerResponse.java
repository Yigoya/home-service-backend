package com.home.service.dto;

import java.util.List;

import com.home.service.models.CustomerAddress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private List<String> serviceHistory;
    private List<CustomerAddress> savedAddresses;

}
