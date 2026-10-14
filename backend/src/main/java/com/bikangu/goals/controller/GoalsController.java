package com.bikangu.goals.controller;

import com.bikangu.goals.entity.SavingsGoal;
import com.bikangu.goals.service.SavingsGoalService;
import com.bikangu.common.response.ApiResponse;
import com.bikangu.authentication.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalsController {

    private final SavingsGoalService goalService;

    @PostMapping
    public ResponseEntity<ApiResponse<SavingsGoal>> createGoal(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SavingsGoalService.CreateGoalRequest request) {

        SavingsGoal goal = goalService.createGoal(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(goal, "Goal created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavingsGoal>>> getGoals(
            @AuthenticationPrincipal User user) {

        List<SavingsGoal> goals = goalService.getUserGoals(user.getId());
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<SavingsGoal>>> getActiveGoals(
            @AuthenticationPrincipal User user) {

        List<SavingsGoal> goals = goalService.getActiveGoals(user.getId());
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<SavingsGoal>>> getUpcomingDeadlines(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "30") int days) {

        List<SavingsGoal> goals = goalService.getUpcomingDeadlines(user.getId(), days);
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponse<SavingsGoal>> getGoal(
            @AuthenticationPrincipal User user,
            @PathVariable UUID goalId) {

        SavingsGoal goal = goalService.getGoal(user.getId(), goalId);
        return ResponseEntity.ok(ApiResponse.success(goal));
    }

    @PatchMapping("/{goalId}")
    public ResponseEntity<ApiResponse<SavingsGoal>> updateGoal(
            @AuthenticationPrincipal User user,
            @PathVariable UUID goalId,
            @Valid @RequestBody SavingsGoalService.UpdateGoalRequest request) {

        SavingsGoal goal = goalService.updateGoal(user.getId(), goalId, request);
        return ResponseEntity.ok(ApiResponse.success(goal, "Goal updated successfully"));
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @AuthenticationPrincipal User user,
            @PathVariable UUID goalId) {

        goalService.deleteGoal(user.getId(), goalId);
        return ResponseEntity.ok(ApiResponse.success(null, "Goal deleted successfully"));
    }
}