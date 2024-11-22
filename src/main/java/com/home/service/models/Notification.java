package com.home.service.models;

import jakarta.persistence.*;
import com.home.service.models.enums.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;
    private String title;
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type; // e.g., SYSTEM, ACTIVITY, ALERT

    private Boolean readStatus = false;

    @Column(nullable = false)
    private LocalDateTime deliveryDate;

    @Column(nullable = true)
    private Long relatedEntityId;
}
