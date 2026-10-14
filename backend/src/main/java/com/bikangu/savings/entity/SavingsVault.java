package com.bikangu.savings.entity;

import com.bikangu.common.domain.BaseEntity;
import com.bikangu.common.enums.VaultStatus;
import com.bikangu.accounts.entity.FinancialAccount;
import com.bikangu.authentication.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "savings_vaults")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsVault extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal targetAmount;

    @Column(name = "current_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "RWF";

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "start_date", nullable = false)
    @Builder.Default
    private LocalDate startDate = LocalDate.now();

    @Column(name = "lock_date")
    private LocalDate lockDate;

    @Column(name = "unlock_date")
    private LocalDate unlockDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private VaultStatus status = VaultStatus.ACTIVE;

    @Column(name = "interest_rate", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal interestRate = BigDecimal.ZERO;

    @Column(name = "interest_calculated_at")
    private Instant interestCalculatedAt;

    @Column(name = "auto_lock", nullable = false)
    @Builder.Default
    private Boolean autoLock = true;

    @Column(name = "allow_early_withdrawal", nullable = false)
    @Builder.Default
    private Boolean allowEarlyWithdrawal = false;

    @Column(name = "early_withdrawal_penalty_rate", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal earlyWithdrawalPenaltyRate = BigDecimal.ZERO;

    @Column(name = "minimum_balance", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal minimumBalance = BigDecimal.ZERO;

    public BigDecimal getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentAmount.divide(targetAmount, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getRemainingAmount() {
        return targetAmount.subtract(currentAmount);
    }

    public boolean isLocked() {
        return status == VaultStatus.LOCKED ||
                (lockDate != null && !LocalDate.now().isBefore(lockDate));
    }

    public boolean canWithdraw() {
        return status == VaultStatus.ACTIVE || status == VaultStatus.UNLOCKED;
    }
}