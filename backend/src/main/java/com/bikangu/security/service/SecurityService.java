package com.bikangu.security.service;

import com.bikangu.common.enums.SecurityEventType;
import com.bikangu.security.entity.SecurityEvent;
import com.bikangu.security.repository.SecurityEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

    private final SecurityEventRepository securityEventRepository;

    @Transactional
    public void logEvent(UUID userId, SecurityEventType eventType, String severity, String description,
                         String ipAddress, String userAgent, String deviceInfo, String location) {
        SecurityEvent event = SecurityEvent.builder()
                .userId(userId)
                .eventType(eventType.name())
                .severity(severity)
                .description(description)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceInfo(deviceInfo)
                .location(location)
                .resolved(false)
                .build();

        securityEventRepository.save(event);

        // Send alert for high severity events
        if ("HIGH".equals(severity) || "CRITICAL".equals(severity)) {
            if (userId != null) {
                // Would send email alert via notification service
                log.warn("High severity security event for user {}: {}", userId, eventType);
            }
        }
    }

    @Transactional
    public void logFailedLogin(String email, String ipAddress, String userAgent, int attemptCount) {
        SecurityEventType type = attemptCount >= 5 ? SecurityEventType.BRUTE_FORCE_ATTEMPT : SecurityEventType.MULTIPLE_FAILED_LOGINS;
        String severity = attemptCount >= 5 ? "HIGH" : "MEDIUM";

        logEvent(null, type, severity,
                String.format("Failed login attempt %d for email %s", attemptCount, email),
                ipAddress, userAgent, null, null);
    }

    @Transactional
    public void logAccountLocked(UUID userId, String ipAddress) {
        logEvent(userId, SecurityEventType.ACCOUNT_LOCKED, "HIGH",
                "Account locked due to multiple failed login attempts",
                ipAddress, null, null, null);
    }

    @Transactional
    public void logSuspiciousActivity(UUID userId, String description, String ipAddress, String userAgent) {
        logEvent(userId, SecurityEventType.SUSPICIOUS_LOGIN, "HIGH",
                description, ipAddress, userAgent, null, null);
    }

    @Transactional
    public void resolveEvent(UUID eventId, UUID resolvedBy) {
        securityEventRepository.findById(eventId).ifPresent(event -> {
            event.setResolved(true);
            event.setResolvedAt(Instant.now());
            event.setResolvedBy(resolvedBy);
            securityEventRepository.save(event);
        });
    }
}