package com.home.service.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.home.service.models.Notification;
import com.home.service.models.User;
import com.home.service.models.enums.NotificationType;
import com.home.service.repositories.NotificationRepository;
import com.home.service.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // Send notification to a user
    public Notification sendNotification(Long recipientId, String message, NotificationType type,
            Long relatedEntityId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReadStatus(false);
        notification.setDeliveryDate(LocalDateTime.now());
        notification.setRelatedEntityId(relatedEntityId);
        return notificationRepository.save(notification);
    }

    // Get unread notifications for a user
    public List<Notification> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return notificationRepository.findByRecipientAndReadStatus(user, false);
    }

    // Filter notifications by type and read status
    public List<Notification> getNotificationsByType(Long userId, NotificationType type, Boolean readStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return notificationRepository.findByRecipientAndTypeAndReadStatus(user, type, readStatus);
    }

    // Mark a notification as read
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        notification.setReadStatus(true);
        notificationRepository.save(notification);
    }
}
