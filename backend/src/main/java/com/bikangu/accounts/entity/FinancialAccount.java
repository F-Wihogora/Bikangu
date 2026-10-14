package com.bikangu.accounts.entity;

import com.bikangu.common.domain.BaseEntity;
import com.bikangu.common.enums.AccountVerificationStatus;
import com.bikangu.providers.entity.FinancialProviderEntity;
import com.bikangu.authentication.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "financial_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialAccount extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private FinancialProviderEntity provider;

    @Column(name = "provider_account_id", nullable = false, length = 255)
    private String providerAccountId;

    @Column(name = "provider_account_type", length = 50)
    private String providerAccountType;

    @Column(name = "account_name", nullable = false, length = 255)
    private String accountName;

    @Column(name = "account_number_masked", length = 50)
    private String accountNumberMasked;

    @Column(name = "account_holder_name", length = 255)
    private String accountHolderName;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "RWF";

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    @Column(name = "last_balance_check_at")
    private Instant lastBalanceCheckAt;

    @Column(name = "current_balance", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "available_balance", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    @Builder.Default
    private AccountVerificationStatus verificationStatus = AccountVerificationStatus.PENDING;

    @Column(name = "verification_attempts")
    @Builder.Default
    private Integer verificationAttempts = 0;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "verification_failed_reason", columnDefinition = "TEXT")
    private String verificationFailedReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;
}