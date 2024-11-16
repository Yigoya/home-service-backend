package com.home.service.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCategory extends BaseEntity {
    private String categoryName;
    private String description;
}
