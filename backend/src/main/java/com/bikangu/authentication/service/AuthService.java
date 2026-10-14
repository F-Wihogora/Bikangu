package com.bikangu.authentication.service;

import com.bikangu.audit.service.AuditService;
import com.bikangu.authentication.entity.RefreshToken;
import com.bikangu.authentication.entity.User;
import com.bikangu.authentication.entity.UserSession;
import com.bikangu.authentication.repository.RefreshTokenRepository;
import com.bikangu.authentication.repository.UserRepository;
import com.bikangu.authentication.repository.UserSessionRepository;
import com.bikangu.common.exception.BikanguException;
import com.bikangu.common.exception.ConflictException;
import com.bikangu.common.exception.ResourceNotFoundException;
import com.bikangu.common.exception.UnauthorizedException;
import com.bikangu.common.exception.ValidationException;
import com.bikangu.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserSessionRepository userSessionRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpService otpService;
    private final AuditService auditService;

    @Value("${app.security.max-login-attempts}")
    private int maxLoginAttempts;

    @Value("${app.security.lockout-duration-minutes}")
    private int lockoutDurationMinutes;

    @Value("${app.security.pin-length}")
    private int pinLength;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered");
        }
        if (request.phoneNumber() != null && userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new ConflictException("Phone number already registered");
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = User.builder()
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .passwordHash(passwordHash)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender())
                .nationality(request.nationality())
                .isActive(true)
                .isEmailVerified(false)
                .isPhoneVerified(false)
                .build();

        // Assign default USER role
        // Role assignment would be done here

        user = userRepository.save(user);

        // Send verification email
        String verificationToken = generateVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        // Send OTP for phone if provided
        if (request.phoneNumber() != null) {
            otpService.sendOtp(request.phoneNumber(), OtpPurpose.PHONE_VERIFICATION);
        }

        auditService.log(
                user.getId(),
                null,
                com.bikangu.common.enums.AuditAction.USER_REGISTERED,
                "user",
                user.getId(),
                null,
                null,
                null,
                null,
                com.bikangu.common.enums.AuditAction.USER_REGISTERED.name()
        );

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                false,
                false,
                "Registration successful. Please verify your email and phone."
        );
    }

    @Transactional
    public TokenResponse login(LoginRequest request, String deviceId, String deviceInfo, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new UnauthorizedException("Invalid credentials");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new UnauthorizedException("Account temporarily locked due to multiple failed attempts");
        }

        // Reset failed attempts on successful login
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // Create tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Save refresh token
        String refreshTokenHash = SecurityUtil.hashToken(refreshToken);
        RefreshToken tokenEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(refreshTokenHash)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpiration()))
                .build();
        refreshTokenRepository.save(tokenEntity);

        // Create session
        UserSession session = UserSession.builder()
                .user(user)
                .refreshToken(tokenEntity)
                .deviceId(deviceId)
                .deviceName(deviceInfo)
                .ipAddress(ipAddress)
                .browser(userAgent)
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpiration()))
                .isCurrent(true)
                .build();
        userSessionRepository.save(session);

        auditService.log(
                user.getId(),
                user.getId(),
                com.bikangu.common.enums.AuditAction.USER_LOGIN,
                "user",
                user.getId(),
                null,
                null,
                ipAddress,
                userAgent,
                null
        );

        return new TokenResponse(accessToken, refreshToken, jwtService.getAccessTokenExpiration());
    }

    @Transactional
    public TokenResponse refreshToken(String refreshToken, String deviceId, String ipAddress, String userAgent) {
        String tokenHash = SecurityUtil.hashToken(refreshToken);

        RefreshToken tokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!tokenEntity.isValid()) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        User user = tokenEntity.getUser();
        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        // Rotate refresh token
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        String newTokenHash = SecurityUtil.hashToken(newRefreshToken);
        RefreshToken newTokenEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(newTokenHash)
                .deviceInfo(tokenEntity.getDeviceInfo())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpiration()))
                .build();
        refreshTokenRepository.save(newTokenEntity);

        // Revoke old token
        refreshTokenRepository.revokeAndReplace(tokenEntity.getId(), Instant.now(), newTokenEntity.getId());

        // Update session
        userSessionRepository.findByRefreshTokenId(tokenEntity.getId()).ifPresent(session -> {
            session.setRefreshToken(newTokenEntity);
            session.setLastActivityAt(Instant.now());
            session.setIpAddress(ipAddress);
            session.setBrowser(userAgent);
            userSessionRepository.save(session);
        });

        return new TokenResponse(newAccessToken, newRefreshToken, jwtService.getAccessTokenExpiration());
    }

    @Transactional
    public void logout(String refreshToken) {
        String tokenHash = SecurityUtil.hashToken(refreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
            userSessionRepository.findByRefreshTokenId(token.getId()).ifPresent(session -> {
                session.setRevokedAt(Instant.now());
                userSessionRepository.save(session);
            });
        });
    }

    @Transactional
    public void logoutAllDevices(UUID userId) {
        Instant now = Instant.now();
        refreshTokenRepository.revokeAllUserTokens(userId, now);
        userSessionRepository.revokeAllUserSessions(userId, now);
    }

    @Transactional
    public void verifyEmail(String token) {
        // Implementation for email verification
    }

    @Transactional
    public void verifyPhone(String phoneNumber, String otp) {
        otpService.verifyOtp(phoneNumber, otp, OtpPurpose.PHONE_VERIFICATION);
        userRepository.findByPhoneNumber(phoneNumber).ifPresent(user -> {
            user.setIsPhoneVerified(true);
            user.setPhoneVerifiedAt(Instant.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));

        String resetToken = generatePasswordResetToken(user);
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Implementation for password reset
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Revoke all refresh tokens
        logoutAllDevices(userId);

        auditService.log(
                userId,
                userId,
                com.bikangu.common.enums.AuditAction.USER_PASSWORD_CHANGED,
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
    public void setPin(UUID userId, String pin) {
        if (pin == null || pin.length() != pinLength || !pin.matches("\\d+")) {
            throw new ValidationException("PIN must be " + pinLength + " digits");
        }
        // Store PIN hash (in practice, use a separate PIN field)
    }

    @Transactional
    public void verifyPin(UUID userId, String pin) {
        // Implementation for PIN verification
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxLoginAttempts) {
            user.setLockedUntil(Instant.now().plusSeconds(lockoutDurationMinutes * 60L));
        }
        userRepository.save(user);
    }

    private String generateVerificationToken(User user) {
        // Generate JWT for email verification
        return jwtService.generateAccessToken(user); // Simplified
    }

    private String generatePasswordResetToken(User user) {
        // Generate JWT for password reset
        return jwtService.generateAccessToken(user); // Simplified
    }

    public record RegisterRequest(
            String email,
            String phoneNumber,
            String password,
            String firstName,
            String lastName,
            java.time.LocalDate dateOfBirth,
            String gender,
            String nationality
    ) {}

    public record LoginRequest(
            String email,
            String password
    ) {}

    public record AuthResponse(
            UUID userId,
            String email,
            String firstName,
            String lastName,
            boolean emailVerified,
            boolean phoneVerified,
            String message
    ) {}

    public record TokenResponse(
            String accessToken,
            String refreshToken,
            long expiresIn
    ) {}

    public enum OtpPurpose {
        PHONE_VERIFICATION,
        PASSWORD_RESET,
        LOGIN,
        WITHDRAWAL,
        PIN_CHANGE
    }
}