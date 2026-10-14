package com.bikangu.notifications.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    @Value("${app.notification.fcm.server-key:}")
    private String serverKey;

    @Value("${app.notification.fcm.sender-id:}")
    private String senderId;

    public void sendNotification(String fcmToken, String title, String body, String data) {
        if (serverKey == null || serverKey.isEmpty()) {
            log.debug("FCM not configured, skipping push notification");
            return;
        }
        // Implementation would use Firebase Admin SDK
        log.debug("Would send FCM to token {}: {} - {}", fcmToken, title, body);
    }

    public void sendMulticastNotification(List<String> fcmTokens, String title, String body, String data) {
        if (serverKey == null || serverKey.isEmpty()) {
            log.debug("FCM not configured, skipping push notification");
            return;
        }
        // Implementation would use Firebase Admin SDK
        log.debug("Would send multicast FCM to {} tokens: {} - {}", fcmTokens.size(), title, body);
    }

    public void subscribeToTopic(String fcmToken, String topic) {
        // Subscribe token to topic
    }

    public void unsubscribeFromTopic(String fcmToken, String topic) {
        // Unsubscribe token from topic
    }
}