package com.bikangu.savings.entity;

import com.bikangu.common.domain.BaseEntity;
import com.bikangu.common.enums.SavingsRuleType;
import com.bikangu.savings.entity.SavingsVault;
import com.bikangu.authentication.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "savings_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsRule extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vault_id", nullable = false)
    private SavingsVault vault;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 30)
    private SavingsRuleType ruleType;

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "fixed_amount", precision = 19, scale = 4)
    private BigDecimal fixedAmount;

    @Column(name = "min_income_threshold", precision = 19, scale = 4)
    private BigDecimal minIncomeThreshold;

    @Column(name = "max_income_threshold", precision = 19, scale = 4)
    private BigDecimal maxIncomeThreshold;

    @Column(name = "income_source", length = 100)
    private String incomeSource;

    @Column(name = "income_sources", columnDefinition = "TEXT[]")
    private String[] incomeSources;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_paused", nullable = false)
    @Builder.Default
    private Boolean isPaused = false;

    @Column(name = "paused_at")
    private Instant pausedAt;

    @Column(name = "pause_reason", columnDefinition = "TEXT")
    private String pauseReason;

    @Column(name = "execution_count")
    @Builder.Default
    private Integer executionCount = 0;

    @Column(name = "last_executed_at")
    private Instant lastExecutedAt;

    @Column(name = "total_saved", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalSaved = BigDecimal.ZERO;

    public boolean isApplicable(BigDecimal incomeAmount, String source) {
        if (!isActive || isPaused) return false;

        // Check income source filter
        if (incomeSource != null && !incomeSource.equalsIgnoreCase(source)) {
            if (incomeSources == null || incomeSources.length == 0 ||
                !java.util.Arrays.asList(incomeSources).contains(source)) {
                return false;
            }
        }

        // Check thresholds
        if (minIncomeThreshold != null && incomeAmount.compareTo(minIncomeThreshold) < 0) {
            return false;
        }
        if (maxIncomeThreshold != null && incomeAmount.compareTo(maxIncomeThreshold) > 0) {
            return false;
        }

        return true;
    }

    public BigDecimal calculateSavingsAmount(BigDecimal incomeAmount) {
        return switch (ruleType) {
            case PERCENTAGE -> {
                if (percentage == null) yield BigDecimal.ZERO;
                yield incomeAmount.multiply(percentage).divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP);
            }
            case FIXED_AMOUNT -> fixedAmount != null ? fixedAmount : BigDecimal.ZERO;
            case CONDITIONAL_PERCENTAGE -> {
                if (percentage == null) yield BigDecimal.ZERO;
                yield incomeAmount.multiply(percentage).divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP);
            }
            case INCOME_SOURCE -> {
                if (percentage == null) yield BigDecimal.ZERO;
                yield incomeAmount.multiply(percentage).divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP);
            }
            case BONUS -> {
                if (percentage == null) yield BigDecimal.ZERO;
                yield incomeAmount.multiply(percentage).divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP);
            }
        };
    }
}