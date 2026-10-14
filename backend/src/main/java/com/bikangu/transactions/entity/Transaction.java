package com.bikangu.transactions.entity;

import com.bikangu.common.domain.BaseEntity;
import com.bikangu.common.enums.TransactionStatus;
import com.bikangu.common.enums.TransactionType;
import com.bikangu.accounts.entity.FinancialAccount;
import com.bikangu.authentication.entity.User;
import com.bikangu.goals.entity.SavingsGoal;
import com.bikangu.providers.entity.FinancialProviderEntity;
import com.bikangu.savings.entity.SavingsVault;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_account_id")
    private FinancialAccount financialAccount;

    @Column(name = "financial_account_id", insertable = false, updatable = false)
    private UUID financialAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vault_id")
    private SavingsVault vault;

    @Column(name = "vault_id", insertable = false, updatable = false)
    private UUID vaultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private SavingsGoal goal;

    @Column(name = "goal_id", insertable = false, updatable = false)
    private UUID goalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private FinancialProviderEntity provider;

    @Column(name = "provider_id", insertable = false, updatable = false)
    private UUID providerId;

    @Column(name = "external_transaction_id", length = 255)
    private String externalTransactionId;

    @Column(name = "idempotency_key", unique = true, length = 255)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private TransactionType transactionType;

    @Column(name = "transaction_category", length = 30)
    private String transactionCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "RWF";

    @Column(name = "exchange_rate", precision = 10, scale = 6)
    @Builder.Default
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "base_amount", precision = 19, scale = 4)
    private BigDecimal baseAmount;

    @Column(name = "base_currency", length = 3)
    private String baseCurrency;

    @Column(name = "fee_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 19, scale = 4)
    private BigDecimal netAmount;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "reference", length = 255)
    private String reference;

    @Column(name = "provider_reference", length = 255)
    private String providerReference;

    @Column(name = "income_source", length = 100)
    private String incomeSource;

    @Column(name = "income_category", length = 100)
    private String incomeCategory;

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;

    @Column(name = "initiated_at")
    private Instant initiatedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    @Builder.Default
    private Integer maxRetries = 3;
}