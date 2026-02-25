package com.home.service.models;

import java.time.LocalDateTime;

import com.home.service.models.enums.InquiryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inquiries")
@Getter
@Setter
public class Inquiry extends BaseEntity {

    @NotBlank(message = "Subject is required")
    private String subject;

    @Column(length = 5000)
    private String message;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = true)
    private Business sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private Business recipient;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    private InquiryStatus status = InquiryStatus.PENDING;

    private LocalDateTime respondedAt;

    // Fallback contact info when sender is anonymous
    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;
}
