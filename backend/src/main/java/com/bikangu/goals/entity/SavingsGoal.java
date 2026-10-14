package com.bikangu.goals.entity;

import com.bikangu.common.domain.BaseEntity;
import com.bikangu.common.enums.GoalStatus;
import com.bikangu.savings.entity.SavingsVault;
import com.bikangu.authentication.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "savings_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsGoal extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vault_id")
    private SavingsVault vault;

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

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "lock_date")
    private LocalDate lockDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private GoalStatus status = GoalStatus.ACTIVE;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "auto_contribute", nullable = false)
    @Builder.Default
    private Boolean autoContribute = false;

    @Column(name = "contribution_frequency", length = 20)
    private String contributionFrequency;

    @Column(name = "contribution_amount", precision = 19, scale = 4)
    private BigDecimal contributionAmount;

    public BigDecimal getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentAmount.divide(targetAmount, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getRemainingAmount() {
        return targetAmount.subtract(currentAmount);
    }

    public boolean isCompleted() {
        return currentAmount.compareTo(targetAmount) >= 0;
    }

    public boolean isOverdue() {
        return deadline != null && LocalDate.now().isAfter(deadline) && !isCompleted();
    }
}