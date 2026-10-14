package com.bikangu.analytics.dto;

import com.bikangu.savings.entity.SavingsVault;
import com.bikangu.goals.entity.SavingsGoal;
import com.bikangu.transactions.entity.Transaction;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    private BigDecimal totalSavings;
    private BigDecimal thisMonthSavings;
    private BigDecimal lockedSavings;
    private BigDecimal availableBalance;
    private int activeGoalsCount;
    private int totalVaultsCount;
    private List<Transaction> recentTransactions;
    private List<SavingsGoal> activeGoals;
    private List<SavingsVault> vaults;
}