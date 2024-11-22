package com.home.service.dto;

import java.time.LocalDateTime;

import com.home.service.models.Notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long recipientId;
    private String title;
    private String message;
    private String type;
    private Boolean readStatus;
    private LocalDateTime deliveryDate;
    private Long relatedEntityId;

    public NotificationDTO(Notification notification) {

        this.id = notification.getId();
        this.recipientId = notification.getRecipient().getId();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.type = notification.getType().name();
        this.readStatus = notification.getReadStatus();
        this.deliveryDate = notification.getDeliveryDate();
        this.relatedEntityId = notification.getRelatedEntityId();

    }
}
