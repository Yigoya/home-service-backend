package com.home.service.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.home.service.dto.NotificationDTO;
import com.home.service.models.Customer;
import com.home.service.models.CustomerSubscription;
import com.home.service.models.Notification;
import com.home.service.models.Tender;
import com.home.service.models.User;
import com.home.service.models.enums.NotificationType;
import com.home.service.models.enums.SubscriptionStatus;
import com.home.service.repositories.CustomerSubscriptionRepository;
import com.home.service.repositories.NotificationRepository;
import com.home.service.repositories.TenderRepository;
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
    private final CustomerSubscriptionRepository subscriptionRepository;
    private final RestTemplate restTemplate;
    private final TenderRepository tenderRepository;

    private final String serverIpAddress = getServerIpAddress();

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
            EmailService emailService, FcmService fcmService, CustomerSubscriptionRepository subscriptionRepository,
            RestTemplate restTemplate,
            TenderRepository tenderRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.fcmService = fcmService;
        this.subscriptionRepository = subscriptionRepository;
        this.restTemplate = restTemplate;
        this.tenderRepository = tenderRepository;

    }

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

    public void sendWhatsAppNotification(String phoneNumber, String message) {
        String url = "https://api.whatsapp.com/send";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("phone", phoneNumber);
        params.add("text", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        try {
            restTemplate.postForObject(url, entity, String.class);
        } catch (Exception e) {
            // Log error
        }
    }

    public void sendTelegramNotification(String username, String message) {
        String telegramApi = "https://api.telegram.org/bot<YOUR_BOT_TOKEN>/sendMessage";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("chat_id", username);
        params.add("text", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        try {
            restTemplate.postForObject(telegramApi, entity, String.class);
        } catch (Exception e) {
            // Log error
        }
    }

    @Async
    public void notifySubscribers(Tender tender) {
        List<CustomerSubscription> subscriptions = subscriptionRepository
                .findByFollowedServiceIdsContainingAndStatus(
                        tender.getService().getId(),
                        SubscriptionStatus.ACTIVE);

        String htmlContent = buildTenderNotificationHtml(tender);
        String plainMessage = buildTenderNotificationText(tender);

        for (CustomerSubscription sub : subscriptions) {
            Customer customer = sub.getCustomer();

            // Email notification
            emailService.sendHtmlEmail(customer.getUser().getEmail(),
                    "New Tender Update: " + tender.getTitle(),
                    htmlContent);

            // WhatsApp notification
            if (sub.getWhatsappNumber() != null) {
                sendWhatsAppNotification(sub.getWhatsappNumber(), plainMessage);
            }

            // Telegram notification
            if (sub.getTelegramUsername() != null) {
                sendTelegramNotification(sub.getTelegramUsername(), plainMessage);
            }
        }
    }

    private String buildTenderNotificationHtml(Tender tender) {
        return "<html><body style='font-family: Arial, sans-serif;'>"
                + "<h2 style='color: #2E86C1;'>New Tender Update</h2>"
                + "<h3>" + tender.getTitle() + "</h3>"
                + "<p>" + tender.getDescription() + "</p>"
                + "<p><strong>Location:</strong> " + tender.getLocation() + "</p>"
                + "<p><strong>Closing Date:</strong> " + tender.getClosingDate() + "</p>"
                + "<p><strong>Status:</strong> " + tender.getStatus() + "</p>"
                + "</body></html>";
    }

    private String buildTenderNotificationText(Tender tender) {
        return "New Tender: " + tender.getTitle() + "\n"
                + "Description: " + tender.getDescription() + "\n"
                + "Location: " + tender.getLocation() + "\n"
                + "Closing Date: " + tender.getClosingDate() + "\n"
                + "Status: " + tender.getStatus();
    }

}
