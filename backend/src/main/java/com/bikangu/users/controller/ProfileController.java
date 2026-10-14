package com.bikangu.users.controller;

import com.bikangu.users.service.UserService;
import com.bikangu.authentication.entity.User;
import com.bikangu.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<User>> getProfile(
            @AuthenticationPrincipal User user) {

        User profile = userService.getProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserService.UpdateProfileRequest request) {

        User updated = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    @PostMapping("/change-pin")
    public ResponseEntity<ApiResponse<Void>> changePin(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserService.ChangePinRequest request) {

        userService.changePin(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "PIN changed successfully"));
    }

    @PostMapping("/biometrics")
    public ResponseEntity<ApiResponse<Void>> toggleBiometrics(
            @AuthenticationPrincipal User user,
            @RequestBody BiometricsRequest request) {

        userService.enableBiometrics(user.getId(), request.enabled());
        return ResponseEntity.ok(ApiResponse.success(null, "Biometrics " + (request.enabled() ? "enabled" : "disabled")));
    }

    @PatchMapping("/notifications")
    public ResponseEntity<ApiResponse<Void>> updateNotificationPreferences(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserService.NotificationPreferencesRequest request) {

        userService.updateNotificationPreferences(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification preferences updated"));
    }

    public record BiometricsRequest(boolean enabled) {}
}