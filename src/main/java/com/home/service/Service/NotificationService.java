package com.home.service.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.home.service.dto.NotificationDTO;
import com.home.service.models.Notification;
import com.home.service.models.User;
import com.home.service.models.enums.NotificationType;
import com.home.service.repositories.NotificationRepository;
import com.home.service.repositories.UserRepository;
import com.home.service.services.EmailService;
import com.home.service.services.FcmService;

import jakarta.persistence.EntityNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final FcmService fcmService;
    private final String serverIpAddress = getServerIpAddress();

    public String getServerIpAddress() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to get the server IP address", e);
        }
    }

    public String getPublicIpAddress() {
        try {
            InetAddress inetAddress = InetAddress.getByName("api.ipify.org");
            System.out.println("Public IP Address: " + inetAddress.getHostAddress());
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            System.out.print("Unable to get the public IP address" + e);
            return null;
        }
    }

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
            EmailService emailService, FcmService fcmService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.fcmService = fcmService;
    }

    // Send notification to a user
    public Notification sendNotification(Long recipientId, String title, String message, NotificationType type,
            Long relatedEntityId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        System.out.println("relatedEntityId: " + relatedEntityId);
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReadStatus(false);
        notification.setDeliveryDate(LocalDateTime.now());
        notification.setRelatedEntityId(relatedEntityId);
        emailService.sendNotificationEmail(recipient, title, message, type);
        String imageUrl = recipient.getProfileImage() != null
                ? "http://" + serverIpAddress + ":8080/uploads/" + recipient.getProfileImage()
                : null;
        fcmService.sendNotification(recipient, title, message, imageUrl, "booking", relatedEntityId.toString());
        return notificationRepository.save(notification);
    }

    // Get unread notifications for a user
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<Notification> notifications = notificationRepository.findByRecipientAndReadStatus(user, false);
        return notifications.stream()
                .map(notification -> new NotificationDTO(notification))
                .toList();
    }

    // Filter notifications by type and read status
    public List<NotificationDTO> getNotificationsByType(Long userId, NotificationType type, Boolean readStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<Notification> notifications = notificationRepository.findByRecipientAndTypeAndReadStatus(user,
                type,
                readStatus);
        return notifications.stream()
                .map(notification -> new NotificationDTO(notification))
                .toList();
    }

    // Mark a notification as read
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        notification.setReadStatus(true);
        notificationRepository.save(notification);
    }

    // Mark all notifications as read
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<Notification> notifications = notificationRepository.findByRecipientAndReadStatus(user, false);
        notifications.forEach(notification -> {
            notification.setReadStatus(true);
            notificationRepository.save(notification);
        });
    }

}
