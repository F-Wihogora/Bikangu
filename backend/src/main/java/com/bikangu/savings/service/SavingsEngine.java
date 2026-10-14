package com.bikangu.savings.service;

import com.bikangu.audit.service.AuditService;
import com.bikangu.accounts.entity.FinancialAccount;
import com.bikangu.accounts.repository.FinancialAccountRepository;
import com.bikangu.common.enums.TransactionStatus;
import com.bikangu.common.enums.TransactionType;
import com.bikangu.common.exception.IdempotencyException;
import com.bikangu.common.exception.InsufficientFundsException;
import com.bikangu.common.exception.ResourceNotFoundException;
import com.bikangu.common.util.FinancialUtil;
import com.bikangu.goals.entity.SavingsGoal;
import com.bikangu.goals.repository.SavingsGoalRepository;
import com.bikangu.notifications.service.NotificationService;
import com.bikangu.providers.model.TransferRequest;
import com.bikangu.providers.model.TransferResponse;
import com.bikangu.providers.service.ProviderService;
import com.bikangu.savings.entity.SavingsRule;
import com.bikangu.savings.entity.SavingsVault;
import com.bikangu.savings.repository.SavingsRuleRepository;
import com.bikangu.savings.repository.SavingsVaultRepository;
import com.bikangu.transactions.entity.IncomeTransaction;
import com.bikangu.transactions.entity.SavingsTransaction;
import com.bikangu.transactions.entity.Transaction;
import com.bikangu.transactions.repository.IncomeTransactionRepository;
import com.bikangu.transactions.repository.SavingsTransactionRepository;
import com.bikangu.transactions.repository.TransactionRepository;
import com.bikangu.audit.service.AuditService;
import com.bikangu.authentication.entity.User;
import com.bikangu.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsEngine {

    private final SavingsRuleRepository ruleRepository;
    private final SavingsVaultRepository vaultRepository;
    private final SavingsGoalRepository goalRepository;
    private final FinancialAccountRepository accountRepository;
    private final UserRepository userRepository;
    private final ProviderService providerService;
    private final TransactionRepository transactionRepository;
    private final IncomeTransactionRepository incomeTransactionRepository;
    private final SavingsTransactionRepository savingsTransactionRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Transactional
    public void processIncomeTransaction(IncomeTransaction incomeTransaction) {
        log.info("Processing income transaction: {}", incomeTransaction.getId());

        // Check if already processed
        if (incomeTransaction.getProcessed()) {
            log.warn("Income transaction already processed: {}", incomeTransaction.getId());
            return;
        }

        // Check idempotency
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(incomeTransaction.getExternalTransactionId());
        if (existing.isPresent()) {
            log.warn("Duplicate income transaction detected: {}", incomeTransaction.getExternalTransactionId());
            throw new IdempotencyException("Income transaction already processed");
        }

        User user = userRepository.findById(incomeTransaction.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", incomeTransaction.getUser().getId()));

        FinancialAccount account = accountRepository.findById(incomeTransaction.getFinancialAccount().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Financial Account", incomeTransaction.getFinancialAccount().getId()));

        // Get applicable savings rules
        List<SavingsRule> rules = ruleRepository.findActiveRulesByUserOrderByPriority(user.getId());

        BigDecimal totalSavings = BigDecimal.ZERO;
        BigDecimal incomeAmount = incomeTransaction.getAmount();

        for (SavingsRule rule : rules) {
            if (rule.isApplicable(incomeAmount, incomeTransaction.getIncomeSource())) {
                BigDecimal savingsAmount = rule.calculateSavingsAmount(incomeAmount);
                if (FinancialUtil.isPositive(savingsAmount)) {
                    totalSavings = FinancialUtil.add(totalSavings, savingsAmount);
                }
            }
        }

        // Check available balance
        if (account.getAvailableBalance().compareTo(totalSavings) < 0) {
            log.warn("Insufficient funds for savings: available={}, required={}",
                    account.getAvailableBalance(), totalSavings);
            throw new InsufficientFundsException("Insufficient funds to execute savings rules");
        }

        // Execute savings for each rule
        for (SavingsRule rule : rules) {
            if (rule.isApplicable(incomeAmount, incomeTransaction.getIncomeSource())) {
                BigDecimal savingsAmount = rule.calculateSavingsAmount(incomeAmount);
                if (FinancialUtil.isPositive(savingsAmount)) {
                    executeSavingsRule(rule, incomeTransaction, savingsAmount);
                }
            }
        }

        // Mark income transaction as processed
        incomeTransaction.setProcessed(true);
        incomeTransaction.setProcessedAt(Instant.now());
        incomeTransaction.setSavingsCalculated(totalSavings);
        incomeTransactionRepository.save(incomeTransaction);

        log.info("Processed income transaction {}: income={}, savings={}",
                incomeTransaction.getId(), incomeAmount, totalSavings);
    }

    @Transactional
    public void executeSavingsRule(SavingsRule rule, IncomeTransaction incomeTransaction, BigDecimal savingsAmount) {
        log.info("Executing savings rule {}: amount={}", rule.getId(), savingsAmount);

        SavingsVault vault = vaultRepository.findById(rule.getVault().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Savings Vault", rule.getVault().getId()));

        FinancialAccount account = accountRepository.findById(incomeTransaction.getFinancialAccount().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Financial Account", incomeTransaction.getFinancialAccount().getId()));

        // Create savings transaction record
        Transaction transaction = Transaction.builder()
                .userId(rule.getUser().getId())
                .financialAccountId(account.getId())
                .vaultId(vault.getId())
                .providerId(account.getProvider().getId())
                .idempotencyKey("SAVINGS_" + incomeTransaction.getExternalTransactionId() + "_" + rule.getId())
                .transactionType(TransactionType.SAVINGS)
                .transactionCategory("AUTOMATIC_SAVINGS")
                .status(TransactionStatus.PROCESSING)
                .amount(savingsAmount)
                .currency(incomeTransaction.getCurrency())
                .netAmount(savingsAmount)
                .description("Automatic savings from " + incomeTransaction.getIncomeSource())
                .reference("SAVINGS_" + incomeTransaction.getExternalTransactionId())
                .incomeSource(incomeTransaction.getIncomeSource())
                .initiatedAt(Instant.now())
                .build();

        transaction = transactionRepository.save(transaction);

        // Create savings transaction detail
        SavingsTransaction savingsTransaction = SavingsTransaction.builder()
                .transactionId(transaction.getId())
                .userId(rule.getUser().getId())
                .vaultId(vault.getId())
                .ruleId(rule.getId())
                .financialAccountId(account.getId())
                .providerId(account.getProvider().getId())
                .amount(savingsAmount)
                .currency(incomeTransaction.getCurrency())
                .netAmount(savingsAmount)
                .status(com.bikangu.common.enums.TransactionStatus.PENDING)
                .initiatedAt(Instant.now())
                .build();

        savingsTransactionRepository.save(savingsTransaction);

        // Initiate transfer through provider
        TransferRequest transferRequest = new TransferRequest(
                transaction.getId(),
                account.getProviderAccountId(),
                vault.getId().toString(), // In real implementation, this would be the vault's provider account
                savingsAmount,
                incomeTransaction.getCurrency(),
                "Automatic savings: " + rule.getName(),
                transaction.getReference()
        );

        try {
            TransferResponse response = providerService.initiateTransfer(account.getProvider().getId(), transferRequest);

            // Update transaction status
            transaction.setExternalTransactionId(response.externalTransferId());
            transaction.setProviderReference(response.externalTransferId());
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setCompletedAt(Instant.now());
            transactionRepository.save(transaction);

            savingsTransaction.setExternalTransferId(response.externalTransferId());
            savingsTransaction.setStatus(com.bikangu.common.enums.TransactionStatus.SUCCESS);
            savingsTransaction.setCompletedAt(Instant.now());
            savingsTransaction.setNetAmount(response.netAmount());
            savingsTransactionRepository.save(savingsTransaction);

            // Update vault balance
            vault.setCurrentAmount(FinancialUtil.add(vault.getCurrentAmount(), savingsAmount));
            vaultRepository.save(vault);

            // Update rule stats
            rule.setExecutionCount(rule.getExecutionCount() + 1);
            rule.setLastExecutedAt(Instant.now());
            rule.setTotalSaved(FinancialUtil.add(rule.getTotalSaved(), savingsAmount));
            ruleRepository.save(rule);

            // Update goal progress if linked
            updateGoalProgress(vault.getId(), savingsAmount);

            // Send notification
            notificationService.sendSavingsExecutedNotification(rule.getUser().getId(), savingsAmount, vault.getName(), rule.getName());

            auditService.log(
                    rule.getUser().getId(),
                    rule.getUser().getId(),
                    com.bikangu.common.enums.AuditAction.SAVINGS_TRANSACTION_EXECUTED,
                    "savings_transaction",
                    transaction.getId(),
                    null,
                    transaction,
                    null,
                    null,
                    null
            );

            log.info("Savings rule executed successfully: rule={}, amount={}, vault={}",
                    rule.getId(), savingsAmount, vault.getId());

        } catch (Exception e) {
            log.error("Savings execution failed for rule {}: {}", rule.getId(), e.getMessage());

            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailedAt(Instant.now());
            transaction.setFailureReason(e.getMessage());
            transactionRepository.save(transaction);

            savingsTransaction.setStatus(com.bikangu.common.enums.TransactionStatus.FAILED);
            savingsTransaction.setFailedAt(Instant.now());
            savingsTransaction.setFailureReason(e.getMessage());
            savingsTransactionRepository.save(savingsTransaction);

            notificationService.sendSavingsFailedNotification(rule.getUser().getId(), savingsAmount, vault.getName(), e.getMessage());

            auditService.log(
                    rule.getUser().getId(),
                    rule.getUser().getId(),
                    com.bikangu.common.enums.AuditAction.SAVINGS_TRANSACTION_FAILED,
                    "savings_transaction",
                    transaction.getId(),
                    null,
                    null,
                    null,
                    null,
                    e.getMessage()
            );

            throw e;
        }
    }

    private void updateGoalProgress(UUID vaultId, BigDecimal amount) {
        List<SavingsGoal> goals = goalRepository.findByVaultId(vaultId);
        for (SavingsGoal goal : goals) {
            if (goal.getStatus() == com.bikangu.common.enums.GoalStatus.ACTIVE) {
                goal.setCurrentAmount(FinancialUtil.add(goal.getCurrentAmount(), amount));
                if (goal.isCompleted()) {
                    goal.setStatus(com.bikangu.common.enums.GoalStatus.COMPLETED);
                    notificationService.sendGoalAchievedNotification(goal.getUser().getId(), goal.getName(), goal.getTargetAmount());
                }
                goalRepository.save(goal);
            }
        }
    }
}