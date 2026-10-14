package com.bikangu.transactions.entity;

import com.bikangu.common.domain.BaseEntity;
import com.bikangu.accounts.entity.FinancialAccount;
import com.bikangu.authentication.entity.User;
import com.bikangu.providers.entity.FinancialProviderEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "income_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeTransaction extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private UUID userId;

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

    @Column(name = "external_transaction_id", nullable = false, length = 255)
    private String externalTransactionId;

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "RWF";

    @Column(name = "income_source", nullable = false, length = 100)
    private String incomeSource;

    @Column(name = "income_category", length = 100)
    private String incomeCategory;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "detected_at", nullable = false)
    @Builder.Default
    private Instant detectedAt = Instant.now();

    @Column(name = "is_qualified", nullable = false)
    @Builder.Default
    private Boolean isQualified = true;

    @Column(name = "qualification_reason", columnDefinition = "TEXT")
    private String qualificationReason;

    @Column(name = "processed", nullable = false)
    @Builder.Default
    private Boolean processed = false;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "savings_calculated", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal savingsCalculated = BigDecimal.ZERO;

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;
}