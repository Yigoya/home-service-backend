package com.home.service.dto;

import com.home.service.models.Operator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OperatorResponse {
    private String assignedRegion;
    private String idCardImage;

    public OperatorResponse(Operator operator) {
        this.assignedRegion = operator.getAssignedRegion();
        this.idCardImage = operator.getIdCardImage();
    }
}
