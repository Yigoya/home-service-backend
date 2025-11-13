package com.home.service.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(name = "saved_businesses", uniqueConstraints = {
    // A customer can only save a specific business once
    @UniqueConstraint(columnNames = {"customer_id", "business_id"})
})
@Data
@NoArgsConstructor
public class SavedBusiness {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @CreationTimestamp
    private Instant savedAt;

    public SavedBusiness(Customer customer, Business business) {
        this.customer = customer;
        this.business = business;
    }
}