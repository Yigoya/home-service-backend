package com.home.service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.home.service.Service.NotificationService;
import com.home.service.models.Notification;
import com.home.service.models.enums.NotificationType;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<Notification> sendNotification(@RequestParam Long recipientId,
            @RequestParam String message,
            @RequestParam NotificationType type,
            @RequestParam(required = false) Long relatedEntityId) {
        Notification notification = notificationService.sendNotification(recipientId, message, type, relatedEntityId);
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@RequestParam Long userId) {
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Notification>> getNotificationsByType(@RequestParam Long userId,
            @RequestParam NotificationType type,
            @RequestParam(required = false, defaultValue = "false") Boolean readStatus) {
        List<Notification> notifications = notificationService.getNotificationsByType(userId, type, readStatus);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/mark-as-read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }
}
