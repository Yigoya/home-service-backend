package com.home.service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.home.service.Service.NotificationService;
import com.home.service.config.exceptions.UserNotFoundException;
import com.home.service.dto.NotificationDTO;
import com.home.service.dto.NotificationRequest;
import com.home.service.models.Notification;
import com.home.service.models.User;
import com.home.service.models.enums.NotificationType;
import com.home.service.repositories.UserRepository;
import com.home.service.services.FcmService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final FcmService fcmService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, FcmService fcmService,
            UserRepository userRepository) {
        this.notificationService = notificationService;
        this.fcmService = fcmService;
        this.userRepository = userRepository;
    }

    @PostMapping("/send")
    public ResponseEntity<Notification> sendNotification(@RequestParam Long recipientId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam NotificationType type,
            @RequestParam(required = false) Long relatedEntityId) {
        Notification notification = notificationService.sendNotification(recipientId, title, message, type,
                relatedEntityId);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/fcm-send")
    public String sendNotification(@RequestBody NotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        fcmService.sendNotification(
                user,
                request.getTitle(),
                request.getBody(),
                request.getImageUrl(),
                request.getTargetPage(),
                null);
        return "Notification sent!";
    }

    @GetMapping("/unread/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByType(@RequestParam Long userId,
            @RequestParam NotificationType type,
            @RequestParam(required = false, defaultValue = "false") Boolean readStatus) {
        List<NotificationDTO> notifications = notificationService.getNotificationsByType(userId, type, readStatus);
        return ResponseEntity.ok(notifications);
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/{id}/mark-as-read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @CrossOrigin(originPatterns = "*")
    @PutMapping("/mark-all-as-read/{userId}")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}
