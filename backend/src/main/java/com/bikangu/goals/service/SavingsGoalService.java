package com.bikangu.goals.service;

import com.bikangu.audit.service.AuditService;
import com.bikangu.common.exception.ResourceNotFoundException;
import com.bikangu.common.exception.ValidationException;
import com.bikangu.goals.entity.SavingsGoal;
import com.bikangu.goals.repository.SavingsGoalRepository;
import com.bikangu.savings.entity.SavingsVault;
import com.bikangu.savings.repository.SavingsVaultRepository;
import com.bikangu.authentication.entity.User;
import com.bikangu.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsGoalService {

    private final SavingsGoalRepository goalRepository;
    private final SavingsVaultRepository vaultRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public SavingsGoal createGoal(UUID userId, CreateGoalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        SavingsVault vault = null;
        if (request.vaultId() != null) {
            vault = vaultRepository.findById(request.vaultId())
                    .orElseThrow(() -> new ResourceNotFoundException("Savings Vault", request.vaultId()));
            if (!vault.getUser().getId().equals(userId)) {
                throw new ValidationException("Vault does not belong to user");
            }
        }

        SavingsGoal goal = SavingsGoal.builder()
                .user(user)
                .vault(vault)
                .name(request.name())
                .description(request.description())
                .targetAmount(request.targetAmount())
                .currency(request.currency())
                .icon(request.icon())
                .color(request.color())
                .deadline(request.deadline())
                .lockDate(request.lockDate())
                .priority(request.priority() != null ? request.priority() : 0)
                .autoContribute(request.autoContribute() != null ? request.autoContribute() : false)
                .contributionFrequency(request.contributionFrequency())
                .contributionAmount(request.contributionAmount())
                .build();

        goal = goalRepository.save(goal);

        auditService.log(
                userId,
                userId,
                com.bikangu.common.enums.AuditAction.SAVINGS_GOAL_CREATED,
                "savings_goal",
                goal.getId(),
                null,
                goal,
                null,
                null,
                null
        );

        return goal;
    }

    @Transactional
    public SavingsGoal updateGoal(UUID userId, UUID goalId, UpdateGoalRequest request) {
        SavingsGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings Goal", goalId));

        if (!goal.getUser().getId().equals(userId)) {
            throw new ValidationException("Goal does not belong to user");
        }

        if (request.name() != null) goal.setName(request.name());
        if (request.description() != null) goal.setDescription(request.description());
        if (request.targetAmount() != null) goal.setTargetAmount(request.targetAmount());
        if (request.icon() != null) goal.setIcon(request.icon());
        if (request.color() != null) goal.setColor(request.color());
        if (request.deadline() != null) goal.setDeadline(request.deadline());
        if (request.lockDate() != null) goal.setLockDate(request.lockDate());
        if (request.priority() != null) goal.setPriority(request.priority());
        if (request.autoContribute() != null) goal.setAutoContribute(request.autoContribute());
        if (request.contributionFrequency() != null) goal.setContributionFrequency(request.contributionFrequency());
        if (request.contributionAmount() != null) goal.setContributionAmount(request.contributionAmount());

        goal = goalRepository.save(goal);

        auditService.log(
                userId,
                userId,
                com.bikangu.common.enums.AuditAction.SAVINGS_GOAL_UPDATED,
                "savings_goal",
                goalId,
                null,
                goal,
                null,
                null,
                null
        );

        return goal;
    }

    @Transactional
    public void deleteGoal(UUID userId, UUID goalId) {
        SavingsGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings Goal", goalId));

        if (!goal.getUser().getId().equals(userId)) {
            throw new ValidationException("Goal does not belong to user");
        }

        goalRepository.delete(goal);
    }

    @Transactional(readOnly = true)
    public List<SavingsGoal> getUserGoals(UUID userId) {
        return goalRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<SavingsGoal> getActiveGoals(UUID userId) {
        return goalRepository.findByUserIdAndStatusIn(userId,
                List.of(com.bikangu.common.enums.GoalStatus.ACTIVE, com.bikangu.common.enums.GoalStatus.OVERDUE));
    }

    @Transactional(readOnly = true)
    public List<SavingsGoal> getUpcomingDeadlines(UUID userId, int days) {
        LocalDate now = LocalDate.now();
        LocalDate future = now.plusDays(days);
        return goalRepository.findUpcomingDeadlines(userId, now, future);
    }

    @Transactional(readOnly = true)
    public SavingsGoal getGoal(UUID userId, UUID goalId) {
        SavingsGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings Goal", goalId));

        if (!goal.getUser().getId().equals(userId)) {
            throw new ValidationException("Goal does not belong to user");
        }

        return goal;
    }

    public record CreateGoalRequest(
            UUID vaultId,
            String name,
            String description,
            BigDecimal targetAmount,
            String currency,
            String icon,
            String color,
            LocalDate deadline,
            LocalDate lockDate,
            Integer priority,
            Boolean autoContribute,
            String contributionFrequency,
            BigDecimal contributionAmount
    ) {}

    public record UpdateGoalRequest(
            String name,
            String description,
            BigDecimal targetAmount,
            String icon,
            String color,
            LocalDate deadline,
            LocalDate lockDate,
            Integer priority,
            Boolean autoContribute,
            String contributionFrequency,
            BigDecimal contributionAmount
    ) {}
}