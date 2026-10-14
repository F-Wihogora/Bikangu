package com.bikangu.analytics.controller;

import com.bikangu.analytics.dto.AnalyticsData;
import com.bikangu.analytics.dto.DashboardSummary;
import com.bikangu.analytics.service.AnalyticsService;
import com.bikangu.common.response.ApiResponse;
import com.bikangu.authentication.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardSummary>> getDashboardSummary(
            @AuthenticationPrincipal User user) {

        DashboardSummary summary = analyticsService.getDashboardSummary(user.getId());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<AnalyticsData>> getAnalytics(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "12") int months) {

        AnalyticsData data = analyticsService.getAnalytics(user.getId(), months);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}