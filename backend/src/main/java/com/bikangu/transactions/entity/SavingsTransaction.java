package com.bikangu.transactions.entity;

import com.bikangu.common.domain.BaseEntity;
import com.bikangu.accounts.entity.FinancialAccount;
import com.bikangu.authentication.entity.User;
import com.bikangu.goals.entity.SavingsGoal;
import com.bikangu.providers.entity.FinancialProviderEntity;
import com.bikangu.savings.entity.SavingsRule;
import com.bikangu.savings.entity.SavingsVault;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "savings_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsTransaction extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    @Column(name = "transaction_id", insertable = false, updatable = false)
    private UUID transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vault_id", nullable = false)
    private SavingsVault vault;

    @Column(name = "vault_id", insertable = false, updatable = false)
    private UUID vaultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private SavingsRule rule;

    @Column(name = "rule_id", insertable = false, updatable = false)
    private UUID ruleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private SavingsGoal goal;

    @Column(name = "goal_id", insertable = false, updatable = false)
    private UUID goalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_account_id", nullable = false)
    private FinancialAccount financialAccount;

    @Column(name = "financial_account_id", insertable = false, updatable = false)
    private UUID financialAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private FinancialProviderEntity provider;

    @Column(name = "provider_id", insertable = false, updatable = false)
    private UUID providerId;

    @Column(name = "external_transfer_id", length = 255)
    private String externalTransferId;

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "RWF";

    @Column(name = "fee_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 19, scale = 4)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private com.bikangu.common.enums.TransactionStatus status = com.bikangu.common.enums.TransactionStatus.PENDING;

    @Column(name = "transfer_status", length = 30)
    private String transferStatus;

    @Column(name = "initiated_at")
    private Instant initiatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "receipt_generated", nullable = false)
    @Builder.Default
    private Boolean receiptGenerated = false;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;
}