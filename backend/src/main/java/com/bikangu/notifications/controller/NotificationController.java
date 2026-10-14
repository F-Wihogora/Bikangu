package com.bikangu.notifications.controller;

import com.bikangu.notifications.entity.Notification;
import com.bikangu.notifications.repository.NotificationRepository;
import com.bikangu.common.response.ApiResponse;
import com.bikangu.authentication.entity.User;
import com.bikangu.common.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Notification>>> getNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean unreadOnly) {

        Page<Notification> notifications;
        if (Boolean.TRUE.equals(unreadOnly)) {
            notifications = notificationRepository.findByUserIdAndIsRead(user.getId(), false,
                    PaginationUtil.createPageable(page, size));
        } else {
            notifications = notificationRepository.findByUserId(user.getId(),
                    PaginationUtil.createPageable(page, size));
        }

        return ResponseEntity.ok(ApiResponse.success(notifications, PaginationUtil.toPagination(notifications)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal User user) {

        long count = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal User user,
            @PathVariable UUID notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new com.bikangu.common.exception.ResourceNotFoundException("Notification", notificationId));

        notification.setIsRead(true);
        notification.setReadAt(java.time.Instant.now());
        notificationRepository.save(notification);

        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal User user) {

        notificationRepository.findByUserIdAndIsReadFalse(user.getId())
                .forEach(n -> {
                    n.setIsRead(true);
                    n.setReadAt(java.time.Instant.now());
                });

        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }
}