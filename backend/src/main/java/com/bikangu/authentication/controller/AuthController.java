package com.bikangu.authentication.controller;

import com.bikangu.authentication.service.AuthService;
import com.bikangu.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthService.AuthResponse>> register(@Valid @RequestBody AuthService.RegisterRequest request) {
        AuthService.AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthService.TokenResponse>> login(
            @Valid @RequestBody AuthService.LoginRequest request,
            HttpServletRequest httpRequest) {

        String deviceId = httpRequest.getHeader("X-Device-Id");
        String deviceInfo = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthService.TokenResponse response = authService.login(request, deviceId, deviceInfo, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthService.TokenResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        String deviceId = httpRequest.getHeader("X-Device-Id");
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthService.TokenResponse response = authService.refreshToken(request.refreshToken(), deviceId, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAllDevices(@RequestAttribute("userId") UUID userId) {
        authService.logoutAllDevices(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out from all devices"));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.token());
        return ResponseEntity.ok(ApiResponse.success(null, "Email verified successfully"));
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResponse<Void>> verifyPhone(@Valid @RequestBody VerifyPhoneRequest request) {
        authService.verifyPhone(request.phoneNumber(), request.otp());
        return ResponseEntity.ok(ApiResponse.success(null, "Phone verified successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset email sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successful"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(userId, request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @PostMapping("/set-pin")
    public ResponseEntity<ApiResponse<Void>> setPin(
            @RequestAttribute("userId") UUID userId,
            @Valid @RequestBody SetPinRequest request) {

        authService.setPin(userId, request.pin());
        return ResponseEntity.ok(ApiResponse.success(null, "PIN set successfully"));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    public record RefreshTokenRequest(String refreshToken) {}
    public record VerifyEmailRequest(String token) {}
    public record VerifyPhoneRequest(String phoneNumber, String otp) {}
    public record ForgotPasswordRequest(String email) {}
    public record ResetPasswordRequest(String token, String newPassword) {}
    public record ChangePasswordRequest(String currentPassword, String newPassword) {}
    public record SetPinRequest(String pin) {}
}