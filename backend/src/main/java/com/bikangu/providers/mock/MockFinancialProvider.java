package com.bikangu.providers.mock;

import com.bikangu.common.enums.TransactionStatus;
import com.bikangu.common.enums.TransactionType;
import com.bikangu.common.util.FinancialUtil;
import com.bikangu.providers.FinancialProvider;
import com.bikangu.providers.model.ProviderAccount;
import com.bikangu.providers.model.ProviderTransaction;
import com.bikangu.providers.model.TransferRequest;
import com.bikangu.providers.model.TransferResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
@Slf4j
public class MockFinancialProvider implements FinancialProvider {

    private final Map<String, MockAccount> accounts = new ConcurrentHashMap<>();
    private final Map<String, MockTransaction> transactions = new ConcurrentHashMap<>();
    private final Map<String, MockTransfer> transfers = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    // Simulated scenarios
    private final Map<String, SimulationScenario> scenarios = new ConcurrentHashMap<>();

    @Override
    public String getProviderId() {
        return "MOCK_PROVIDER";
    }

    @Override
    public String getProviderName() {
        return "Mock Financial Provider";
    }

    @Override
    public boolean isSandbox() {
        return true;
    }

    @Override
    public ProviderAccount verifyAccount(String accountId, String credentials) {
        MockAccount account = accounts.computeIfAbsent(accountId, id -> createMockAccount(id));
        account.setVerified(true);
        log.info("Mock provider: Verified account {}", accountId);
        return toProviderAccount(account);
    }

    @Override
    public ProviderAccount getBalance(String accountId) {
        MockAccount account = accounts.computeIfAbsent(accountId, id -> createMockAccount(id));
        log.info("Mock provider: Balance check for account {}: {}", accountId, account.getBalance());
        return toProviderAccount(account);
    }

    @Override
    public List<ProviderTransaction> getTransactions(String accountId, int limit, String cursor) {
        MockAccount account = accounts.computeIfAbsent(accountId, id -> createMockAccount(id));
        List<ProviderTransaction> result = account.getTransactions().stream()
                .limit(limit)
                .map(this::toProviderTransaction)
                .toList();
        log.info("Mock provider: Retrieved {} transactions for account {}", result.size(), accountId);
        return result;
    }

    @Override
    public List<ProviderTransaction> detectIncome(String accountId, Instant since) {
        MockAccount account = accounts.computeIfAbsent(accountId, id -> createMockAccount(id));
        List<ProviderTransaction> income = account.getTransactions().stream()
                .filter(t -> t.getTransactionDate().isAfter(since))
                .filter(t -> TransactionType.INCOME.name().equals(t.getType()))
                .map(this::toProviderTransaction)
                .toList();
        log.info("Mock provider: Detected {} income transactions for account {} since {}", income.size(), accountId, since);
        return income;
    }

    @Override
    public TransferResponse initiateTransfer(TransferRequest request) {
        String transferId = "MOCK_TXN_" + idGenerator.getAndIncrement();
        String externalTransferId = "EXT_" + transferId;

        // Check for simulation scenario
        SimulationScenario scenario = scenarios.get(request.idempotencyKey().toString());
        if (scenario != null) {
            return handleScenario(scenario, request, externalTransferId);
        }

        MockAccount fromAccount = accounts.computeIfAbsent(request.fromAccountId(), id -> createMockAccount(id));
        if (fromAccount.getBalance().compareTo(request.amount()) < 0) {
            return createFailedTransfer(transferId, externalTransferId, request, "Insufficient funds");
        }

        // Simulate processing delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Deduct from source account
        fromAccount.setBalance(FinancialUtil.subtract(fromAccount.getBalance(), request.amount()));

        // Create transfer record
        MockTransfer transfer = new MockTransfer(
                transferId,
                externalTransferId,
                request.fromAccountId(),
                request.toAccountId(),
                request.amount(),
                request.currency(),
                FinancialUtil.toBigDecimal("0"),
                request.amount(),
                TransactionStatus.SUCCESS,
                Instant.now(),
                Instant.now().plusSeconds(2),
                null
        );
        transfers.put(transferId, transfer);

        log.info("Mock provider: Transfer {} completed: {} {} from {} to {}",
                transferId, request.amount(), request.currency(), request.fromAccountId(), request.toAccountId());

        return toTransferResponse(transfer);
    }

    @Override
    public TransferResponse getTransferStatus(String externalTransferId) {
        return transfers.values().stream()
                .filter(t -> t.getExternalTransferId().equals(externalTransferId))
                .findFirst()
                .map(this::toTransferResponse)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + externalTransferId));
    }

    @Override
    public void handleWebhook(String payload, String signature) {
        log.info("Mock provider: Received webhook: {}", payload);
        // In mock, we just log
    }

    @Override
    public boolean isHealthy() {
        return true;
    }

    // Simulation methods for testing
    public void simulateSalaryReceived(String accountId, BigDecimal amount, String source) {
        MockAccount account = accounts.computeIfAbsent(accountId, id -> createMockAccount(id));
        String txnId = "MOCK_INCOME_" + idGenerator.getAndIncrement();
        String externalTxnId = "EXT_INCOME_" + txnId;

        MockTransaction transaction = new MockTransaction(
                txnId,
                externalTxnId,
                TransactionType.INCOME.name(),
                "SALARY",
                amount,
                "RWF",
                "Salary from " + source,
                "SALARY_" + System.currentTimeMillis(),
                Instant.now(),
                TransactionStatus.SUCCESS.name(),
                source,
                "SALARY"
        );
        transactions.put(txnId, transaction);
        account.addTransaction(transaction);
        account.setBalance(FinancialUtil.add(account.getBalance(), amount));

        log.info("Mock provider: Simulated salary of {} {} for account {} from {}",
                amount, "RWF", accountId, source);
    }

    public void simulateFreelancePayment(String accountId, BigDecimal amount, String source) {
        MockAccount account = accounts.computeIfAbsent(accountId, id -> createMockAccount(id));
        String txnId = "MOCK_INCOME_" + idGenerator.getAndIncrement();
        String externalTxnId = "EXT_INCOME_" + txnId;

        MockTransaction transaction = new MockTransaction(
                txnId,
                externalTxnId,
                TransactionType.INCOME.name(),
                "FREELANCE",
                amount,
                "RWF",
                "Freelance payment from " + source,
                "FREELANCE_" + System.currentTimeMillis(),
                Instant.now(),
                TransactionStatus.SUCCESS.name(),
                source,
                "FREELANCE"
        );
        transactions.put(txnId, transaction);
        account.addTransaction(transaction);
        account.setBalance(FinancialUtil.add(account.getBalance(), amount));

        log.info("Mock provider: Simulated freelance payment of {} {} for account {} from {}",
                amount, "RWF", accountId, source);
    }

    public void simulateBonus(String accountId, BigDecimal amount, String source) {
        MockAccount account = accounts.computeIfAbsent(accountId, id -> createMockAccount(id));
        String txnId = "MOCK_INCOME_" + idGenerator.getAndIncrement();
        String externalTxnId = "EXT_INCOME_" + txnId;

        MockTransaction transaction = new MockTransaction(
                txnId,
                externalTxnId,
                TransactionType.INCOME.name(),
                "BONUS",
                amount,
                "RWF",
                "Bonus from " + source,
                "BONUS_" + System.currentTimeMillis(),
                Instant.now(),
                TransactionStatus.SUCCESS.name(),
                source,
                "BONUS"
        );
        transactions.put(txnId, transaction);
        account.addTransaction(transaction);
        account.setBalance(FinancialUtil.add(account.getBalance(), amount));

        log.info("Mock provider: Simulated bonus of {} {} for account {} from {}",
                amount, "RWF", accountId, source);
    }

    public void setSimulationScenario(String idempotencyKey, SimulationScenario scenario) {
        scenarios.put(idempotencyKey, scenario);
    }

    public void clearSimulationScenario(String idempotencyKey) {
        scenarios.remove(idempotencyKey);
    }

    // Helper methods
    private MockAccount createMockAccount(String accountId) {
        return new MockAccount(
                accountId,
                "ACC_" + accountId,
                "MOBILE_MONEY",
                "Mock Account " + accountId,
                "****" + accountId.substring(Math.max(0, accountId.length() - 4)),
                "Mock User",
                "RWF",
                FinancialUtil.toBigDecimal("1000000"),
                FinancialUtil.toBigDecimal("1000000"),
                true,
                true,
                Instant.now(),
                new ArrayList<>()
        );
    }

    private ProviderAccount toProviderAccount(MockAccount account) {
        return new ProviderAccount(
                UUID.randomUUID(),
                account.getAccountId(),
                account.getAccountType(),
                account.getAccountName(),
                account.getAccountNumberMasked(),
                account.getAccountHolderName(),
                account.getCurrency(),
                account.getBalance(),
                account.getAvailableBalance(),
                account.isVerified(),
                account.isActive(),
                account.getLastSyncAt(),
                account.getTransactions().stream().limit(10).map(this::toProviderTransaction).toList()
        );
    }

    private ProviderTransaction toProviderTransaction(MockTransaction txn) {
        return new ProviderTransaction(
                UUID.fromString(txn.getId().replace("MOCK_", "00000000-0000-0000-0000-")),
                txn.getExternalTransactionId(),
                txn.getType(),
                txn.getCategory(),
                txn.getAmount(),
                txn.getCurrency(),
                txn.getDescription(),
                txn.getReference(),
                txn.getTransactionDate(),
                txn.getStatus(),
                txn.getIncomeSource(),
                txn.getIncomeCategory()
        );
    }

    private TransferResponse toTransferResponse(MockTransfer transfer) {
        return new TransferResponse(
                UUID.fromString(transfer.getId().replace("MOCK_", "00000000-0000-0000-0000-")),
                transfer.getExternalTransferId(),
                transfer.getStatus().name(),
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getFeeAmount(),
                transfer.getNetAmount(),
                transfer.getInitiatedAt(),
                transfer.getCompletedAt(),
                transfer.getFailureReason()
        );
    }

    private TransferResponse handleScenario(SimulationScenario scenario, TransferRequest request, String externalTransferId) {
        String transferId = "MOCK_TXN_" + idGenerator.getAndIncrement();

        return switch (scenario) {
            case SUCCESS -> {
                MockAccount fromAccount = accounts.get(request.fromAccountId());
                if (fromAccount != null) {
                    fromAccount.setBalance(FinancialUtil.subtract(fromAccount.getBalance(), request.amount()));
                }
                MockTransfer transfer = new MockTransfer(
                        transferId, externalTransferId, request.fromAccountId(), request.toAccountId(),
                        request.amount(), request.currency(),
                        FinancialUtil.toBigDecimal("0"), request.amount(),
                        TransactionStatus.SUCCESS, Instant.now(), Instant.now().plusSeconds(2), null
                );
                transfers.put(transferId, transfer);
                yield toTransferResponse(transfer);
            }
            case INSUFFICIENT_FUNDS -> createFailedTransfer(transferId, externalTransferId, request, "Insufficient funds");
            case PROVIDER_TIMEOUT -> createFailedTransfer(transferId, externalTransferId, request, "Provider timeout");
            case TRANSFER_FAILED -> createFailedTransfer(transferId, externalTransferId, request, "Transfer failed by provider");
            default -> createFailedTransfer(transferId, externalTransferId, request, "Unknown scenario");
        };
    }

    private TransferResponse createFailedTransfer(String transferId, String externalTransferId, TransferRequest request, String reason) {
        MockTransfer transfer = new MockTransfer(
                transferId, externalTransferId, request.fromAccountId(), request.toAccountId(),
                request.amount(), request.currency(),
                FinancialUtil.toBigDecimal("0"), FinancialUtil.toBigDecimal("0"),
                TransactionStatus.FAILED, Instant.now(), null, reason
        );
        transfers.put(transferId, transfer);
        return toTransferResponse(transfer);
    }

    // Internal classes - using regular classes instead of records for mutability
    private static class MockAccount {
        private String id;
        private String accountId;
        private String accountType;
        private String accountName;
        private String accountNumberMasked;
        private String accountHolderName;
        private String currency;
        private BigDecimal balance;
        private BigDecimal availableBalance;
        private boolean verified;
        private boolean active;
        private Instant lastSyncAt;
        private List<MockTransaction> transactions;

        public MockAccount(String id, String accountId, String accountType, String accountName,
                          String accountNumberMasked, String accountHolderName, String currency,
                          BigDecimal balance, BigDecimal availableBalance, boolean verified,
                          boolean active, Instant lastSyncAt, List<MockTransaction> transactions) {
            this.id = id;
            this.accountId = accountId;
            this.accountType = accountType;
            this.accountName = accountName;
            this.accountNumberMasked = accountNumberMasked;
            this.accountHolderName = accountHolderName;
            this.currency = currency;
            this.balance = balance;
            this.availableBalance = availableBalance;
            this.verified = verified;
            this.active = active;
            this.lastSyncAt = lastSyncAt;
            this.transactions = transactions;
        }

        public void addTransaction(MockTransaction txn) {
            transactions.add(0, txn);
        }
        public String getId() { return id; }
        public String getAccountId() { return accountId; }
        public String getAccountType() { return accountType; }
        public String getAccountName() { return accountName; }
        public String getAccountNumberMasked() { return accountNumberMasked; }
        public String getAccountHolderName() { return accountHolderName; }
        public String getCurrency() { return currency; }
        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }
        public BigDecimal getAvailableBalance() { return availableBalance; }
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }
        public boolean isActive() { return active; }
        public Instant getLastSyncAt() { return lastSyncAt; }
        public List<MockTransaction> getTransactions() { return transactions; }
    }

    private static class MockTransaction {
        private String id;
        private String externalTransactionId;
        private String type;
        private String category;
        private BigDecimal amount;
        private String currency;
        private String description;
        private String reference;
        private Instant transactionDate;
        private String status;
        private String incomeSource;
        private String incomeCategory;

        public MockTransaction(String id, String externalTransactionId, String type, String category,
                              BigDecimal amount, String currency, String description, String reference,
                              Instant transactionDate, String status, String incomeSource, String incomeCategory) {
            this.id = id;
            this.externalTransactionId = externalTransactionId;
            this.type = type;
            this.category = category;
            this.amount = amount;
            this.currency = currency;
            this.description = description;
            this.reference = reference;
            this.transactionDate = transactionDate;
            this.status = status;
            this.incomeSource = incomeSource;
            this.incomeCategory = incomeCategory;
        }

        public String getId() { return id; }
        public String getExternalTransactionId() { return externalTransactionId; }
        public String getType() { return type; }
        public String getCategory() { return category; }
        public BigDecimal getAmount() { return amount; }
        public String getCurrency() { return currency; }
        public String getDescription() { return description; }
        public String getReference() { return reference; }
        public Instant getTransactionDate() { return transactionDate; }
        public String getStatus() { return status; }
        public String getIncomeSource() { return incomeSource; }
        public String getIncomeCategory() { return incomeCategory; }
    }

    private static class MockTransfer {
        private String id;
        private String externalTransferId;
        private String fromAccountId;
        private String toAccountId;
        private BigDecimal amount;
        private String currency;
        private BigDecimal feeAmount;
        private BigDecimal netAmount;
        private TransactionStatus status;
        private Instant initiatedAt;
        private Instant completedAt;
        private String failureReason;

        public MockTransfer(String id, String externalTransferId, String fromAccountId, String toAccountId,
                           BigDecimal amount, String currency, BigDecimal feeAmount, BigDecimal netAmount,
                           TransactionStatus status, Instant initiatedAt, Instant completedAt, String failureReason) {
            this.id = id;
            this.externalTransferId = externalTransferId;
            this.fromAccountId = fromAccountId;
            this.toAccountId = toAccountId;
            this.amount = amount;
            this.currency = currency;
            this.feeAmount = feeAmount;
            this.netAmount = netAmount;
            this.status = status;
            this.initiatedAt = initiatedAt;
            this.completedAt = completedAt;
            this.failureReason = failureReason;
        }

        public String getId() { return id; }
        public String getExternalTransferId() { return externalTransferId; }
        public String getFromAccountId() { return fromAccountId; }
        public String getToAccountId() { return toAccountId; }
        public BigDecimal getAmount() { return amount; }
        public String getCurrency() { return currency; }
        public BigDecimal getFeeAmount() { return feeAmount; }
        public BigDecimal getNetAmount() { return netAmount; }
        public TransactionStatus getStatus() { return status; }
        public Instant getInitiatedAt() { return initiatedAt; }
        public Instant getCompletedAt() { return completedAt; }
        public String getFailureReason() { return failureReason; }
    }

    public enum SimulationScenario {
        SUCCESS,
        INSUFFICIENT_FUNDS,
        PROVIDER_TIMEOUT,
        TRANSFER_FAILED
    }
}