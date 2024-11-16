package com.home.service.Service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.home.service.models.Notification;
import com.home.service.models.enums.NotificationType;
import com.home.service.repositories.NotificationRepository;

@Service
public class WebSocketNotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(NotificationRepository notificationRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public Notification sendNotification(Long recipientId, String message, NotificationType type,
            Long relatedEntityId) {
        Notification notification = new Notification();
        notification.setMessage("this is message");

        // Send WebSocket message to the specific user
        messagingTemplate.convertAndSend("/topic/user/" + recipientId, notification);

        return notification;
    }
}
