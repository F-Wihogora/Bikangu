package com.bikangu.providers.controller;

import com.bikangu.common.enums.TransactionStatus;
import com.bikangu.common.enums.TransactionType;
import com.bikangu.common.util.FinancialUtil;
import com.bikangu.providers.service.ProviderService;
import com.bikangu.providers.entity.FinancialProviderEntity;
import com.bikangu.providers.repository.FinancialProviderRepository;
import com.bikangu.accounts.entity.FinancialAccount;
import com.bikangu.accounts.repository.FinancialAccountRepository;
import com.bikangu.transactions.entity.IncomeTransaction;
import com.bikangu.transactions.entity.Transaction;
import com.bikangu.transactions.repository.IncomeTransactionRepository;
import com.bikangu.transactions.repository.TransactionRepository;
import com.bikangu.savings.service.SavingsEngine;
import com.bikangu.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final ProviderService providerService;
    private final FinancialProviderRepository providerRepository;
    private final FinancialAccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IncomeTransactionRepository incomeTransactionRepository;
    private final SavingsEngine savingsEngine;

    @PostMapping("/{providerId}")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(
            @PathVariable UUID providerId,
            @RequestBody String payload,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestHeader(value = "X-Event-Type", required = false) String eventType) {

        log.info("Received webhook from provider {}: eventType={}", providerId, eventType);

        FinancialProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new com.bikangu.common.exception.ResourceNotFoundException("Provider", providerId));

        try {
            providerService.handleWebhook(providerId, payload, signature);

            // Process based on event type
            if ("transaction.created".equals(eventType) || "income.received".equals(eventType)) {
                processIncomeWebhook(provider, payload);
            }

            return ResponseEntity.ok(ApiResponse.success(null, "Webhook processed"));
        } catch (Exception e) {
            log.error("Webhook processing failed for provider {}: {}", providerId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Webhook processing failed: " + e.getMessage()));
        }
    }

    private void processIncomeWebhook(FinancialProviderEntity provider, String payload) {
        // Parse webhook payload (simplified - in reality would use proper JSON parsing)
        // For mock provider, we simulate the income detection flow
        log.info("Processing income webhook for provider: {}", provider.getName());

        // In a real implementation, this would:
        // 1. Parse the webhook payload
        // 2. Extract transaction details
        // 3. Find the corresponding financial account
        // 4. Create IncomeTransaction record
        // 5. Trigger savings engine
    }

    @PostMapping("/mock/simulate-income")
    public ResponseEntity<ApiResponse<String>> simulateIncome(
            @RequestParam UUID accountId,
            @RequestParam String amount,
            @RequestParam String source,
            @RequestParam(required = false) String category) {

        FinancialAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new com.bikangu.common.exception.ResourceNotFoundException("Account", accountId));

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .userId(account.getUser().getId())
                .financialAccountId(account.getId())
                .providerId(account.getProvider().getId())
                .externalTransactionId("MOCK_INCOME_" + System.currentTimeMillis())
                .idempotencyKey("IDEMPOTENCY_" + System.currentTimeMillis())
                .transactionType(TransactionType.INCOME)
                .transactionCategory(category != null ? category : "SALARY")
                .status(TransactionStatus.SUCCESS)
                .amount(FinancialUtil.toBigDecimal(amount))
                .currency("RWF")
                .netAmount(FinancialUtil.toBigDecimal(amount))
                .description("Simulated income from " + source)
                .reference("SIM_INCOME_" + System.currentTimeMillis())
                .incomeSource(source)
                .incomeCategory(category)
                .completedAt(Instant.now())
                .build();

        transaction = transactionRepository.save(transaction);

        // Create income transaction
        IncomeTransaction incomeTransaction = IncomeTransaction.builder()
                .transaction(transaction)
                .userId(account.getUser().getId())
                .financialAccountId(account.getId())
                .providerId(account.getProvider().getId())
                .externalTransactionId(transaction.getExternalTransactionId())
                .amount(FinancialUtil.toBigDecimal(amount))
                .currency("RWF")
                .incomeSource(source)
                .incomeCategory(category)
                .receivedAt(Instant.now())
                .isQualified(true)
                .build();

        incomeTransactionRepository.save(incomeTransaction);

        // Process through savings engine
        savingsEngine.processIncomeTransaction(incomeTransaction);

        return ResponseEntity.ok(ApiResponse.success("Income simulated and processed", "Income processed successfully"));
    }

    @PostMapping("/mock/simulate-salary")
    public ResponseEntity<ApiResponse<String>> simulateSalary(
            @RequestParam UUID accountId,
            @RequestParam String amount) {

        return simulateIncome(accountId, amount, "EMPLOYER", "SALARY");
    }

    @PostMapping("/mock/simulate-freelance")
    public ResponseEntity<ApiResponse<String>> simulateFreelance(
            @RequestParam UUID accountId,
            @RequestParam String amount,
            @RequestParam String client) {

        return simulateIncome(accountId, amount, client, "FREELANCE");
    }

    @PostMapping("/mock/simulate-bonus")
    public ResponseEntity<ApiResponse<String>> simulateBonus(
            @RequestParam UUID accountId,
            @RequestParam String amount,
            @RequestParam String source) {

        return simulateIncome(accountId, amount, source, "BONUS");
    }
}