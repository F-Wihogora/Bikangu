package com.bikangu.notifications.service;

import com.bikangu.authentication.service.EmailService;
import com.bikangu.common.enums.NotificationType;
import com.bikangu.notifications.entity.Notification;
import com.bikangu.notifications.repository.NotificationRepository;
import com.bikangu.authentication.entity.User;
import com.bikangu.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;
    private final EmailService emailService;

    @Transactional
    public Notification createNotification(UUID userId, NotificationType type, String title, String message) {
        return createNotification(userId, type, title, message, null, null, null);
    }

    @Transactional
    public Notification createNotification(UUID userId, NotificationType type, String title, String message,
                                           String actionUrl, String actionLabel, String data) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .category(getCategory(type))
                .title(title)
                .message(message)
                .priority("NORMAL")
                .channels(new String[]{"IN_APP"})
                .data(data)
                .actionUrl(actionUrl)
                .actionLabel(actionLabel)
                .isRead(false)
                .sentAt(Instant.now())
                .build();

        notification = notificationRepository.save(notification);

        // Send push notification asynchronously
        sendPushNotification(user, notification);

        // Send email if needed
        if (shouldSendEmail(type)) {
            sendEmailNotification(user, notification);
        }

        return notification;
    }

    @Transactional
    public void sendSavingsExecutedNotification(UUID userId, java.math.BigDecimal amount, String vaultName, String ruleName) {
        String title = "Savings Successful";
        String message = String.format("RWF %,.0f has been saved to your %s vault via %s rule.",
                amount, vaultName, ruleName);
        createNotification(userId, NotificationType.SAVINGS_EXECUTED, title, message);
    }

    @Transactional
    public void sendSavingsFailedNotification(UUID userId, java.math.BigDecimal amount, String vaultName, String reason) {
        String title = "Savings Failed";
        String message = String.format("Failed to save RWF %,.0f to %s: %s", amount, vaultName, reason);
        createNotification(userId, NotificationType.SAVINGS_FAILED, title, message, null, "HIGH", null);
    }

    @Transactional
    public void sendGoalAchievedNotification(UUID userId, String goalName, java.math.BigDecimal targetAmount) {
        String title = "Goal Achieved! ���";
        String message = String.format("Congratulations! You've reached your savings goal '%s' of RWF %,.0f.", goalName, targetAmount);
        createNotification(userId, NotificationType.GOAL_ACHIEVED, title, message);
    }

    @Transactional
    public void sendIncomeReceivedNotification(UUID userId, java.math.BigDecimal amount, String source) {
        String title = "Income Received";
        String message = String.format("RWF %,.0f received from %s. Your savings rules have been applied.", amount, source);
        createNotification(userId, NotificationType.SALARY_RECEIVED, title, message);
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
        unread.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(Instant.now());
        });
        notificationRepository.saveAll(unread);
    }

    private String getCategory(NotificationType type) {
        return switch (type) {
            case SALARY_RECEIVED, SAVINGS_EXECUTED, SAVINGS_FAILED, GOAL_ACHIEVED, GOAL_NEARING_DEADLINE -> "SAVINGS";
            case ACCOUNT_CONNECTED, ACCOUNT_DISCONNECTED -> "ACCOUNT";
            case SECURITY_ALERT, FAILED_LOGIN, PASSWORD_CHANGED, PIN_CHANGED, NEW_DEVICE_LOGIN, ACCOUNT_LOCKED, ACCOUNT_UNLOCKED -> "SECURITY";
            case WITHDRAWAL_REQUESTED, TRANSFER_COMPLETED -> "TRANSACTION";
            case WEEKLY_SUMMARY, MONTHLY_REPORT -> "REPORT";
            case KYC_APPROVED, KYC_REJECTED -> "KYC";
            default -> "GENERAL";
        };
    }

    private boolean shouldSendEmail(NotificationType type) {
        return type == NotificationType.SECURITY_ALERT ||
                type == NotificationType.GOAL_ACHIEVED ||
                type == NotificationType.KYC_APPROVED ||
                type == NotificationType.KYC_REJECTED ||
                type == NotificationType.WITHDRAWAL_REQUESTED;
    }

    private void sendPushNotification(User user, Notification notification) {
        // Would integrate with FCM
        log.debug("Would send push notification to user {}: {}", user.getId(), notification.getTitle());
    }

    private void sendEmailNotification(User user, Notification notification) {
        // Would send email
        log.debug("Would send email notification to user {}: {}", user.getId(), notification.getTitle());
    }
}