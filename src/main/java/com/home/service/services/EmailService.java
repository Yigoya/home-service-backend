package com.home.service.services;

import java.time.LocalDateTime;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.home.service.models.PasswordResetToken;
import com.home.service.models.User;
import com.home.service.models.VerificationToken;
import com.home.service.repositories.PasswordResetTokenRepository;
import com.home.service.repositories.VerificationTokenRepository;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    public void sendVerifyEmail(User user) {
        // Generate verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, LocalDateTime.now().plusMinutes(30), user);
        verificationTokenRepository.save(verificationToken);

        String subject = "Account Verification";
        String text = "To verify your email, click the link below:\nhttp://localhost:8090/auth/verify?token="
                + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendDeclineEmail(User user) {
        String subject = "Application Declined";
        String text = "Dear " + user.getName() + ",\n\n"
                + "We regret to inform you that your application to join our platform has been declined after careful review. "
                + "For more information, please feel free to reach out to our support team.\n\n"
                + "Thank you for your interest.\n\n"
                + "Best regards,\n"
                + "Support Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendResetPassEmail(User user) {

        // Generate verification token
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, LocalDateTime.now().plusMinutes(30),
                user);
        passwordResetTokenRepository.save(passwordResetToken);

        String subject = "Password Reset Request";
        String text = "To verify your email, click the link below:\nhttp://localhost:8090/auth/reset-password?token="
                + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendTechnicianVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, LocalDateTime.now().plusMinutes(30), user);
        verificationTokenRepository.save(verificationToken);

        String subject = "Technician Account Verification";
        String text = "Congratulations! Your application has been accepted. Please verify your account by clicking the link below:\n"
                +
                "http://localhost:8090/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendApprovalEmail(User user) {
        String subject = "Account Approved";
        String text = "Dear " + user.getName() + ",\n\n"
                + "Congratulations! Your account has been activated.\n\n"
                + "Welcome to our platform!\n\n"
                + "Best regards,\nSupport Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendRejectionEmail(User user) {
        String subject = "Document Declined";
        String text = "Dear " + user.getName() + ",\n\n"
                + "We regret to inform you that your submitted payment proof has been declined.\n\n"
                + "Please contact support for more information.\n\n"
                + "Best regards,\nSupport Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
