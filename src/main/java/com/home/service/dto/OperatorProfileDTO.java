package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.home.service.models.Operator;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperatorProfileDTO {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;

    public OperatorProfileDTO(Operator operator) {

        // initialize fields using the operator object
        this.id = operator.getId();
        this.name = operator.getUser().getName();
        this.email = operator.getUser().getEmail();
        this.phoneNumber = operator.getUser().getPhoneNumber();

    }

}
