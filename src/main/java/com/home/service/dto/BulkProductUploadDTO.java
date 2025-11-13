package com.home.service.dto;

import java.util.List;

import lombok.Data;

@Data
public class BulkProductUploadDTO {
    private List<ProductDTO> products;
}