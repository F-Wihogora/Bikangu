package com.bikangu.accounts.controller;

import com.bikangu.accounts.entity.FinancialAccount;
import com.bikangu.accounts.service.AccountService;
import com.bikangu.common.response.ApiResponse;
import com.bikangu.providers.entity.FinancialProviderEntity;
import com.bikangu.providers.service.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.bikangu.authentication.entity.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final ProviderService providerService;

    @PostMapping
    public ResponseEntity<ApiResponse<FinancialAccount>> linkAccount(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AccountService.LinkAccountRequest request) {

        FinancialAccount account = accountService.linkAccount(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(account, "Account linked successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FinancialAccount>>> getAccounts(
            @AuthenticationPrincipal User user) {

        List<FinancialAccount> accounts = accountService.getUserAccounts(user.getId());
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/primary")
    public ResponseEntity<ApiResponse<FinancialAccount>> getPrimaryAccount(
            @AuthenticationPrincipal User user) {

        return accountService.getPrimaryAccount(user.getId())
                .map(account -> ResponseEntity.ok(ApiResponse.success(account)))
                .orElse(ResponseEntity.ok(ApiResponse.success(null, "No primary account set")));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<FinancialAccount>> getAccount(
            @AuthenticationPrincipal User user,
            @PathVariable UUID accountId) {

        FinancialAccount account = accountService.getUserAccounts(user.getId()).stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new com.bikangu.common.exception.ResourceNotFoundException("Account", accountId));

        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @PatchMapping("/{accountId}")
    public ResponseEntity<ApiResponse<FinancialAccount>> updateAccount(
            @AuthenticationPrincipal User user,
            @PathVariable UUID accountId,
            @Valid @RequestBody AccountService.UpdateAccountRequest request) {

        FinancialAccount account = accountService.updateAccount(user.getId(), accountId, request);
        return ResponseEntity.ok(ApiResponse.success(account, "Account updated successfully"));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<ApiResponse<Void>> unlinkAccount(
            @AuthenticationPrincipal User user,
            @PathVariable UUID accountId) {

        accountService.unlinkAccount(user.getId(), accountId);
        return ResponseEntity.ok(ApiResponse.success(null, "Account unlinked successfully"));
    }

    @PostMapping("/{accountId}/sync")
    public ResponseEntity<ApiResponse<FinancialAccount>> syncBalance(
            @AuthenticationPrincipal User user,
            @PathVariable UUID accountId) {

        FinancialAccount account = accountService.syncBalance(user.getId(), accountId);
        return ResponseEntity.ok(ApiResponse.success(account, "Balance synced successfully"));
    }

    @PostMapping("/{accountId}/refresh")
    public ResponseEntity<ApiResponse<FinancialAccount>> refreshAccount(
            @AuthenticationPrincipal User user,
            @PathVariable UUID accountId) {

        FinancialAccount account = accountService.refreshAccount(user.getId(), accountId);
        return ResponseEntity.ok(ApiResponse.success(account, "Account refreshed successfully"));
    }

    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<List<FinancialProviderEntity>>> getProviders() {
        List<FinancialProviderEntity> providers = providerService.getAllProviders();
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    @GetMapping("/providers/country/{countryCode}")
    public ResponseEntity<ApiResponse<List<FinancialProviderEntity>>> getProvidersByCountry(
            @PathVariable String countryCode) {

        List<FinancialProviderEntity> providers = providerService.getProvidersByCountry(countryCode);
        return ResponseEntity.ok(ApiResponse.success(providers));
    }
}