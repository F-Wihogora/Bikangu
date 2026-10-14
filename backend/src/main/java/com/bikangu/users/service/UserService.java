package com.bikangu.users.service;

import com.bikangu.audit.service.AuditService;
import com.bikangu.authentication.entity.User;
import com.bikangu.authentication.repository.UserRepository;
import com.bikangu.common.exception.ResourceNotFoundException;
import com.bikangu.common.exception.ValidationException;
import com.bikangu.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public User getProfile(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    @Transactional
    public User updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.dateOfBirth() != null) user.setDateOfBirth(request.dateOfBirth());
        if (request.gender() != null) user.setGender(request.gender());
        if (request.nationality() != null) user.setNationality(request.nationality());
        if (request.preferredLanguage() != null) user.setPreferredLanguage(request.preferredLanguage());
        if (request.preferredCurrency() != null) user.setPreferredCurrency(request.preferredCurrency());
        if (request.timezone() != null) user.setTimezone(request.timezone());

        user = userRepository.save(user);

        auditService.log(
                userId,
                userId,
                com.bikangu.common.enums.AuditAction.USER_PROFILE_UPDATED,
                "user",
                userId,
                null,
                user,
                null,
                null,
                null
        );

        return user;
    }

    @Transactional
    public void changePin(UUID userId, ChangePinRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // In a real implementation, verify current PIN and hash new PIN
        // user.setPinHash(SecurityUtil.hashPin(request.newPin()));
        userRepository.save(user);

        auditService.log(
                userId,
                userId,
                com.bikangu.common.enums.AuditAction.USER_PIN_CHANGED,
                "user",
                userId,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Transactional
    public void enableBiometrics(UUID userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Store biometric preference
        // user.setBiometricsEnabled(enabled);
        userRepository.save(user);
    }

    @Transactional
    public void updateNotificationPreferences(UUID userId, NotificationPreferencesRequest request) {
        // Implementation would update notification preferences table
    }

    public record UpdateProfileRequest(
            String firstName,
            String lastName,
            java.time.LocalDate dateOfBirth,
            String gender,
            String nationality,
            String preferredLanguage,
            String preferredCurrency,
            String timezone
    ) {}

    public record ChangePinRequest(
            String currentPin,
            String newPin
    ) {}

    public record NotificationPreferencesRequest(
            Boolean emailEnabled,
            Boolean pushEnabled,
            Boolean inAppEnabled,
            Boolean smsEnabled,
            Boolean salaryReceived,
            Boolean savingsExecuted,
            Boolean savingsFailed,
            Boolean goalAchieved,
            Boolean goalNearingDeadline,
            Boolean accountConnected,
            Boolean accountDisconnected,
            Boolean securityAlerts,
            Boolean failedLogin,
            Boolean passwordChanged,
            Boolean pinChanged,
            Boolean withdrawalRequested,
            Boolean transferCompleted,
            Boolean weeklySummary,
            Boolean monthlyReport,
            Boolean marketingEmails,
            String quietHoursStart,
            String quietHoursEnd,
            String timezone
    ) {}
}