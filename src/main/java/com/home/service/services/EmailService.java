package com.home.service.services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.home.service.models.PasswordResetToken;
import com.home.service.models.User;
import com.home.service.models.VerificationToken;
import com.home.service.models.enums.NotificationType;
import com.home.service.repositories.PasswordResetTokenRepository;
import com.home.service.repositories.VerificationTokenRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    // Role-based base URLs (can be overridden via application.yml)
    @org.springframework.beans.factory.annotation.Value("${app.urls.base.default:https://www.hulumoya.com}")
    private String defaultBaseUrl;
    @org.springframework.beans.factory.annotation.Value("${app.urls.base.customer:https://www.hulumoya.com}")
    private String customerBaseUrl;
    @org.springframework.beans.factory.annotation.Value("${app.urls.base.technician:https://www.hulumoya.com}")
    private String technicianBaseUrl;
    @org.springframework.beans.factory.annotation.Value("${app.urls.base.operator:https://www.hulumoya.com}")
    private String operatorBaseUrl;
    @org.springframework.beans.factory.annotation.Value("${app.urls.base.admin:https://www.hulumoya.com}")
    private String adminBaseUrl;
    @org.springframework.beans.factory.annotation.Value("${app.urls.base.user:https://www.hulumoya.com}")
    private String genericUserBaseUrl;
    @org.springframework.beans.factory.annotation.Value("${app.urls.base.business:https://www.hulumoya.com}")
    private String businessBaseUrl;
    @org.springframework.beans.factory.annotation.Value("${app.urls.base.agency:https://www.hulumoya.com}")
    private String agencyBaseUrl;
    @org.springframework.beans.factory.annotation.Value("${app.urls.base.job_company:https://www.hulumoya.com}")
    private String jobCompanyBaseUrl;
    @org.springframework.beans.factory.annotation.Value("${app.urls.base.job_seeker:https://www.hulumoya.com}")
    private String jobSeekerBaseUrl;

    private String getBaseUrlForRole(com.home.service.models.enums.UserRole role) {
        if (role == null) return defaultBaseUrl;
        switch (role) {
            case CUSTOMER:
                return customerBaseUrl != null ? customerBaseUrl : defaultBaseUrl;
            case TECHNICIAN:
                return technicianBaseUrl != null ? technicianBaseUrl : defaultBaseUrl;
            case OPERATOR:
                return operatorBaseUrl != null ? operatorBaseUrl : defaultBaseUrl;
            case ADMIN:
                return adminBaseUrl != null ? adminBaseUrl : defaultBaseUrl;
            case USER:
                return genericUserBaseUrl != null ? genericUserBaseUrl : defaultBaseUrl;
            case BUSINESS:
                return businessBaseUrl != null ? businessBaseUrl : defaultBaseUrl;
            case AGENCY:
                return agencyBaseUrl != null ? agencyBaseUrl : defaultBaseUrl;
            case JOB_COMPANY:
                return jobCompanyBaseUrl != null ? jobCompanyBaseUrl : defaultBaseUrl;
            case JOB_SEEKER:
                return jobSeekerBaseUrl != null ? jobSeekerBaseUrl : defaultBaseUrl;
            default:
                return defaultBaseUrl;
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailSendException("Failed to send email", e);
        }
    }

    public VerificationToken createOrRefreshVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        String code = String.format("%06d", (int)(Math.random() * 1000000));
        // Upsert token for user to avoid unique constraint violation
        Optional<VerificationToken> optionalToken = verificationTokenRepository.findByUser(user);
        VerificationToken verificationToken;
        if (optionalToken.isPresent()) {
            verificationToken = optionalToken.get();
            verificationToken.setToken(token);
            verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
            verificationToken.setCode(code);
        } else {
            verificationToken = new VerificationToken(token, LocalDateTime.now().plusMinutes(30), user);
            verificationToken.setCode(code);
        }
        return verificationTokenRepository.save(verificationToken);
    }

    public void sendVerifyEmail(User user) {
        VerificationToken verificationToken = createOrRefreshVerificationToken(user);
        sendVerifyEmail(user, verificationToken);
    }

    public void sendVerifyEmail(User user, VerificationToken verificationToken) {
        String subject = "Account Verification";
        String baseUrl = getBaseUrlForRole(user.getRole());
        String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>"
            + "<h2 style='color: #2E86C1;'>Account Verification</h2>"
            + "<p>To verify your account, click the link below or use this 6-digit code in the mobile app:</p>"
            + "<pre style='background-color: #f4f4f4; padding: 10px; border-radius: 5px; font-family: monospace;'>" + verificationToken.getCode() + "</pre>"
            + "<a href='" + baseUrl + "/auth/verify?token=" + verificationToken.getToken()
            + "' style='color: #2E86C1;'>Verify Email</a>"
            + "</body></html>";

        sendHtmlEmail(user.getEmail(), subject, htmlContent);
    }

    public void sendDeclineEmail(User user) {
        String subject = "Application Declined";
        String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>"
                + "<h2 style='color: #C0392B;'>Application Declined</h2>"
                + "<p>Dear " + user.getName() + ",</p>"
                + "<p>We regret to inform you that your application to join our platform has been declined after careful review. "
                + "For more information, please feel free to reach out to our support team.</p>"
                + "<p>Thank you for your interest.</p>"
                + "<p>Best regards,<br>Support Team</p>"
                + "</body></html>";

        sendHtmlEmail(user.getEmail(), subject, htmlContent);
    }

    public void sendResetPassEmail(User user) {
        PasswordResetToken passwordResetToken = createOrRefreshPasswordResetToken(user);
        sendResetPassEmail(user, passwordResetToken);
    }

    public void sendResetPassEmail(User user, PasswordResetToken passwordResetToken) {
        String token = passwordResetToken.getToken();
        String code = passwordResetToken.getCode();

        String subject = "Password Reset Request";
        String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>"
                + "<h2 style='color: #2E86C1;'>Password Reset Request</h2>"
            + "<p>To reset your password, click the link below, use this 6-digit code, or copy the token if you are using a mobile app:</p>"
            + "<pre style='background-color: #f4f4f4; padding: 10px; border-radius: 5px; font-family: monospace;'>"
            + code
            + "</pre>"
                + "<pre style='background-color: #f4f4f4; padding: 10px; border-radius: 5px; font-family: monospace;'>"
                + token
                + "</pre>"
                + "<a href='http://188.245.43.110/reset-password?token=" + token
                + "' style='color: #2E86C1;'>Reset Password</a>"
                + "</body></html>";

        sendHtmlEmail(user.getEmail(), subject, htmlContent);
    }

    public PasswordResetToken createOrRefreshPasswordResetToken(User user) {
        String token = UUID.randomUUID().toString();
        String code = String.format("%06d", (int)(Math.random() * 1000000));
        PasswordResetToken existingToken = passwordResetTokenRepository.findByUser(user);
        if (existingToken != null) {
            existingToken.setToken(token);
            existingToken.setCode(code);
            existingToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
            return passwordResetTokenRepository.save(existingToken);
        }
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, code,
                LocalDateTime.now().plusMinutes(30), user);
        return passwordResetTokenRepository.save(passwordResetToken);
    }

    public void sendTechnicianVerificationEmail(User user) {
        VerificationToken verificationToken = createOrRefreshVerificationToken(user);
        String subject = "Technician Account Verification";
        String baseUrlTech = getBaseUrlForRole(com.home.service.models.enums.UserRole.TECHNICIAN);
        String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>"
            + "<h2 style='color: #2E86C1;'>Technician Account Verification</h2>"
            + "<p>Please verify your technician account using this code in the app or the link below:</p>"
            + "<pre style='background-color: #f4f4f4; padding: 10px; border-radius: 5px; font-family: monospace;'>" + verificationToken.getCode() + "</pre>"
            + "<a href='" + baseUrlTech + "/auth/verify?token=" + verificationToken.getToken() + "' style='color: #2E86C1;'>Verify Account</a>"
            + "</body></html>";

        sendHtmlEmail(user.getEmail(), subject, htmlContent);
    }

    public void sendApprovalEmail(User user) {
        String subject = "Account Approved";
        String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>"
                + "<h2 style='color: #28B463;'>Account Approved</h2>"
                + "<p>Dear " + user.getName() + ",</p>"
                + "<p>Congratulations! Your account has been activated.</p>"
                + "<p>Welcome to our platform!</p>"
                + "<p>Best regards,<br>Support Team</p>"
                + "</body></html>";

        sendHtmlEmail(user.getEmail(), subject, htmlContent);
    }

    public void sendRejectionEmail(User user) {
        String subject = "Document Declined";
        String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>"
                + "<h2 style='color: #C0392B;'>Document Declined</h2>"
                + "<p>Dear " + user.getName() + ",</p>"
                + "<p>We regret to inform you that your submitted payment proof has been declined.</p>"
                + "<p>Please contact support for more information.</p>"
                + "<p>Best regards,<br>Support Team</p>"
                + "</body></html>";

        sendHtmlEmail(user.getEmail(), subject, htmlContent);
    }

    public void sendVerifyEmailForChange(User user, String token) {
        String subject = "Verify Your New Email Address";
        String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>"
                + "<h2 style='color: #2E86C1;'>Verify Your New Email Address</h2>"
                + "<p>Please confirm your new email address by clicking the link below:</p>"
                + "<a href='http://188.245.43.110/verify-email-change?token=" + token
                + "' style='color: #2E86C1;'>Verify Email</a>"
                + "</body></html>";

        sendHtmlEmail(user.getPendingEmail(), subject, htmlContent);
    }

    public void sendNotificationEmail(User user, String title, String message, NotificationType notificationType) {
        String color;
        switch (notificationType) {
            case BOOKING_REQUEST:
                color = "#28B463"; // Green
                break;
            case NotificationType.BOOKING_ACCEPTANCE:
                color = "#F39C12"; // Orange
                break;
            case NotificationType.BOOKING_START:
                color = "#2E86C1"; // Blue
                break;
            default:
                color = "#C0392B"; // Red
        }

        String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>"
                + "<h2 style='color: " + color + ";'>" + title + "</h2>"
                + "<p>" + message + "</p>"
                + "<p>Best regards,<br>Support Team</p>"
                + "</body></html>";

        sendHtmlEmail(user.getEmail(), title, htmlContent);
    }
}
