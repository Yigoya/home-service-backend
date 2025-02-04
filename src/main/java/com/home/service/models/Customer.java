package com.home.service.models;

import jakarta.persistence.*;
import java.util.List;

import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerAddress> savedAddresses;

    @ElementCollection
    private List<String> serviceHistory;

    private Integer coinBalance = 0;
}
