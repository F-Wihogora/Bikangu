package com.bikangu.savings.controller;

import com.bikangu.savings.entity.SavingsRule;
import com.bikangu.savings.entity.SavingsVault;
import com.bikangu.savings.service.SavingsRuleService;
import com.bikangu.savings.service.SavingsVaultService;
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
@RequestMapping("/savings")
@RequiredArgsConstructor
public class SavingsController {

    private final SavingsVaultService vaultService;
    private final SavingsRuleService ruleService;

    // Vault endpoints
    @PostMapping("/vaults")
    public ResponseEntity<ApiResponse<SavingsVault>> createVault(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SavingsVaultService.CreateVaultRequest request) {

        SavingsVault vault = vaultService.createVault(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(vault, "Vault created successfully"));
    }

    @GetMapping("/vaults")
    public ResponseEntity<ApiResponse<List<SavingsVault>>> getVaults(
            @AuthenticationPrincipal User user) {

        List<SavingsVault> vaults = vaultService.getUserVaults(user.getId());
        return ResponseEntity.ok(ApiResponse.success(vaults));
    }

    @GetMapping("/vaults/active")
    public ResponseEntity<ApiResponse<List<SavingsVault>>> getActiveVaults(
            @AuthenticationPrincipal User user) {

        List<SavingsVault> vaults = vaultService.getActiveVaults(user.getId());
        return ResponseEntity.ok(ApiResponse.success(vaults));
    }

    @GetMapping("/vaults/locked")
    public ResponseEntity<ApiResponse<List<SavingsVault>>> getLockedVaults(
            @AuthenticationPrincipal User user) {

        List<SavingsVault> vaults = vaultService.getLockedVaults(user.getId());
        return ResponseEntity.ok(ApiResponse.success(vaults));
    }

    @GetMapping("/vaults/{vaultId}")
    public ResponseEntity<ApiResponse<SavingsVault>> getVault(
            @AuthenticationPrincipal User user,
            @PathVariable UUID vaultId) {

        SavingsVault vault = vaultService.getVault(user.getId(), vaultId);
        return ResponseEntity.ok(ApiResponse.success(vault));
    }

    @PatchMapping("/vaults/{vaultId}")
    public ResponseEntity<ApiResponse<SavingsVault>> updateVault(
            @AuthenticationPrincipal User user,
            @PathVariable UUID vaultId,
            @Valid @RequestBody SavingsVaultService.UpdateVaultRequest request) {

        SavingsVault vault = vaultService.updateVault(user.getId(), vaultId, request);
        return ResponseEntity.ok(ApiResponse.success(vault, "Vault updated successfully"));
    }

    @DeleteMapping("/vaults/{vaultId}")
    public ResponseEntity<ApiResponse<Void>> closeVault(
            @AuthenticationPrincipal User user,
            @PathVariable UUID vaultId) {

        vaultService.closeVault(user.getId(), vaultId);
        return ResponseEntity.ok(ApiResponse.success(null, "Vault closed successfully"));
    }

    // Rule endpoints
    @PostMapping("/rules")
    public ResponseEntity<ApiResponse<SavingsRule>> createRule(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SavingsRuleService.CreateRuleRequest request) {

        SavingsRule rule = ruleService.createRule(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(rule, "Savings rule created successfully"));
    }

    @GetMapping("/rules")
    public ResponseEntity<ApiResponse<List<SavingsRule>>> getRules(
            @AuthenticationPrincipal User user) {

        List<SavingsRule> rules = ruleService.getUserRules(user.getId());
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @GetMapping("/rules/active")
    public ResponseEntity<ApiResponse<List<SavingsRule>>> getActiveRules(
            @AuthenticationPrincipal User user) {

        List<SavingsRule> rules = ruleService.getActiveRules(user.getId());
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @PatchMapping("/rules/{ruleId}")
    public ResponseEntity<ApiResponse<SavingsRule>> updateRule(
            @AuthenticationPrincipal User user,
            @PathVariable UUID ruleId,
            @Valid @RequestBody SavingsRuleService.UpdateRuleRequest request) {

        SavingsRule rule = ruleService.updateRule(user.getId(), ruleId, request);
        return ResponseEntity.ok(ApiResponse.success(rule, "Rule updated successfully"));
    }

    @PostMapping("/rules/{ruleId}/pause")
    public ResponseEntity<ApiResponse<SavingsRule>> pauseRule(
            @AuthenticationPrincipal User user,
            @PathVariable UUID ruleId,
            @RequestBody PauseRuleRequest request) {

        SavingsRule rule = ruleService.pauseRule(user.getId(), ruleId, request.reason());
        return ResponseEntity.ok(ApiResponse.success(rule, "Rule paused successfully"));
    }

    @PostMapping("/rules/{ruleId}/resume")
    public ResponseEntity<ApiResponse<SavingsRule>> resumeRule(
            @AuthenticationPrincipal User user,
            @PathVariable UUID ruleId) {

        SavingsRule rule = ruleService.resumeRule(user.getId(), ruleId);
        return ResponseEntity.ok(ApiResponse.success(rule, "Rule resumed successfully"));
    }

    @DeleteMapping("/rules/{ruleId}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(
            @AuthenticationPrincipal User user,
            @PathVariable UUID ruleId) {

        ruleService.deleteRule(user.getId(), ruleId);
        return ResponseEntity.ok(ApiResponse.success(null, "Rule deleted successfully"));
    }

    public record PauseRuleRequest(String reason) {}
}