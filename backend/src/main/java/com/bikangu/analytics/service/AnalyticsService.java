package com.bikangu.analytics.service;

import com.bikangu.analytics.dto.AnalyticsData;
import com.bikangu.analytics.dto.DashboardSummary;
import com.bikangu.analytics.dto.MonthlySavings;
import com.bikangu.analytics.dto.SavingsByCategory;
import com.bikangu.accounts.repository.FinancialAccountRepository;
import com.bikangu.savings.entity.SavingsVault;
import com.bikangu.savings.repository.SavingsVaultRepository;
import com.bikangu.goals.entity.SavingsGoal;
import com.bikangu.goals.repository.SavingsGoalRepository;
import com.bikangu.transactions.entity.Transaction;
import com.bikangu.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final SavingsVaultRepository vaultRepository;
    private final SavingsGoalRepository goalRepository;
    private final FinancialAccountRepository accountRepository;

    @Transactional(readOnly = true)
    public DashboardSummary getDashboardSummary(UUID userId) {
        // Get total savings across all vaults
        List<SavingsVault> vaults = vaultRepository.findByUserId(userId);
        BigDecimal totalSavings = vaults.stream()
                .map(SavingsVault::getCurrentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get this month's savings
        YearMonth currentMonth = YearMonth.now();
        Instant startOfMonth = currentMonth.atDay(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        Instant endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59).toInstant(java.time.ZoneOffset.UTC);

        BigDecimal thisMonthSavings = transactionRepository.findByUserIdAndDateRange(
                        userId, startOfMonth, endOfMonth, org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .filter(t -> t.getTransactionType() == com.bikangu.common.enums.TransactionType.SAVINGS)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get locked savings
        BigDecimal lockedSavings = vaults.stream()
                .filter(SavingsVault::isLocked)
                .map(SavingsVault::getCurrentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get available balance from primary account
        BigDecimal availableBalance = accountRepository.findByUserIdAndIsPrimaryTrue(userId)
                .map(a -> a.getAvailableBalance())
                .orElse(BigDecimal.ZERO);

        // Get active goals
        List<SavingsGoal> activeGoals = goalRepository.findByUserIdAndStatusIn(userId,
                List.of(com.bikangu.common.enums.GoalStatus.ACTIVE, com.bikangu.common.enums.GoalStatus.OVERDUE));

        // Get recent transactions
        List<Transaction> recentTransactions = transactionRepository.findByUserId(userId,
                org.springframework.data.domain.PageRequest.of(0, 10)).getContent();

        return DashboardSummary.builder()
                .totalSavings(totalSavings)
                .thisMonthSavings(thisMonthSavings)
                .lockedSavings(lockedSavings)
                .availableBalance(availableBalance)
                .activeGoalsCount(activeGoals.size())
                .totalVaultsCount(vaults.size())
                .recentTransactions(recentTransactions)
                .activeGoals(activeGoals)
                .vaults(vaults)
                .build();
    }

    @Transactional(readOnly = true)
    public AnalyticsData getAnalytics(UUID userId, int months) {
        YearMonth endMonth = YearMonth.now();
        YearMonth startMonth = endMonth.minusMonths(months - 1);

        List<MonthlySavings> monthlySavings = new ArrayList<>();
        List<SavingsByCategory> savingsByCategory = new ArrayList<>();

        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            Instant start = current.atDay(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
            Instant end = current.atEndOfMonth().atTime(23, 59, 59).toInstant(java.time.ZoneOffset.UTC);

            List<Transaction> transactions = transactionRepository.findByUserIdAndDateRange(
                    userId, start, end, org.springframework.data.domain.Pageable.unpaged()).getContent();

            BigDecimal income = transactions.stream()
                    .filter(t -> t.getTransactionType() == com.bikangu.common.enums.TransactionType.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal savings = transactions.stream()
                    .filter(t -> t.getTransactionType() == com.bikangu.common.enums.TransactionType.SAVINGS)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal withdrawals = transactions.stream()
                    .filter(t -> t.getTransactionType() == com.bikangu.common.enums.TransactionType.WITHDRAWAL)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            monthlySavings.add(MonthlySavings.builder()
                    .month(current.toString())
                    .income(income)
                    .savings(savings)
                    .withdrawals(withdrawals)
                    .netSavings(savings.subtract(withdrawals))
                    .savingsRate(income.compareTo(BigDecimal.ZERO) > 0
                            ? savings.divide(income, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100))
                            : BigDecimal.ZERO)
                    .build());

            current = current.plusMonths(1);
        }

        // Savings by category
        Map<String, BigDecimal> categoryMap = transactionRepository.findByUserId(userId,
                org.springframework.data.domain.Pageable.unpaged()).getContent().stream()
                .filter(t -> t.getTransactionType() == com.bikangu.common.enums.TransactionType.SAVINGS)
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionCategory() != null ? t.getTransactionCategory() : "OTHER",
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        categoryMap.forEach((category, amount) -> savingsByCategory.add(
                SavingsByCategory.builder().category(category).amount(amount).build()
        ));

        return AnalyticsData.builder()
                .monthlySavings(monthlySavings)
                .savingsByCategory(savingsByCategory)
                .build();
    }
}