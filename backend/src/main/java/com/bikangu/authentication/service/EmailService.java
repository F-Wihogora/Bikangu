package com.bikangu.authentication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Bikangu}")
    private String appName;

    public void sendVerificationEmail(String to, String token) {
        String subject = "Verify your " + appName + " account";
        String body = String.format("""
                Welcome to %s!
                
                Please verify your email address by clicking the link below:
                %s/verify-email?token=%s
                
                This link will expire in 24 hours.
                
                If you didn't create an account, please ignore this email.
                
                Best regards,
                The %s Team
                """, appName, getFrontendUrl(), token, appName);

        sendEmail(to, subject, body);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Reset your " + appName + " password";
        String body = String.format("""
                You requested a password reset for your %s account.
                
                Click the link below to reset your password:
                %s/reset-password?token=%s
                
                This link will expire in 1 hour.
                
                If you didn't request this, please ignore this email.
                
                Best regards,
                The %s Team
                """, appName, getFrontendUrl(), token, appName);

        sendEmail(to, subject, body);
    }

    public void sendWelcomeEmail(String to, String firstName) {
        String subject = "Welcome to " + appName + "!";
        String body = String.format("""
                Hi %s,
                
                Welcome to %s - your smart savings companion!
                
                We're excited to help you build better saving habits automatically.
                
                Get started by:
                1. Linking your financial account
                2. Creating your first savings goal
                3. Setting up automatic savings rules
                
                If you have any questions, our support team is here to help.
                
                Best regards,
                The %s Team
                """, firstName, appName, appName);

        sendEmail(to, subject, body);
    }

    public void sendSecurityAlertEmail(String to, String alertType, String details) {
        String subject = "Security Alert: " + alertType;
        String body = String.format("""
                Hello,
                
                We detected a security event on your %s account:
                
                Type: %s
                Details: %s
                Time: %s
                
                If this wasn't you, please secure your account immediately by:
                1. Changing your password
                2. Reviewing active sessions
                3. Contacting support
                
                Best regards,
                The %s Security Team
                """, appName, alertType, details, java.time.Instant.now(), appName);

        sendEmail(to, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String getFrontendUrl() {
        return System.getenv().getOrDefault("FRONTEND_URL", "https://app.bikangu.com");
    }
}