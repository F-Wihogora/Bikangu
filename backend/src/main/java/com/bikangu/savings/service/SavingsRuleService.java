package com.bikangu.savings.service;

import com.bikangu.audit.service.AuditService;
import com.bikangu.common.exception.ResourceNotFoundException;
import com.bikangu.common.exception.ValidationException;
import com.bikangu.savings.entity.SavingsRule;
import com.bikangu.savings.entity.SavingsVault;
import com.bikangu.savings.repository.SavingsRuleRepository;
import com.bikangu.savings.repository.SavingsVaultRepository;
import com.bikangu.authentication.entity.User;
import com.bikangu.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsRuleService {

    private final SavingsRuleRepository ruleRepository;
    private final SavingsVaultRepository vaultRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public SavingsRule createRule(UUID userId, CreateRuleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        SavingsVault vault = vaultRepository.findById(request.vaultId())
                .orElseThrow(() -> new ResourceNotFoundException("Savings Vault", request.vaultId()));

        if (!vault.getUser().getId().equals(userId)) {
            throw new ValidationException("Vault does not belong to user");
        }

        validateRuleRequest(request);

        SavingsRule rule = SavingsRule.builder()
                .user(user)
                .vault(vault)
                .name(request.name())
                .description(request.description())
                .ruleType(request.ruleType())
                .percentage(request.percentage())
                .fixedAmount(request.fixedAmount())
                .minIncomeThreshold(request.minIncomeThreshold())
                .maxIncomeThreshold(request.maxIncomeThreshold())
                .incomeSource(request.incomeSource())
                .incomeSources(request.incomeSources())
                .priority(request.priority())
                .isActive(true)
                .isPaused(false)
                .build();

        rule = ruleRepository.save(rule);

        auditService.log(
                userId,
                userId,
                com.bikangu.common.enums.AuditAction.SAVINGS_RULE_CREATED,
                "savings_rule",
                rule.getId(),
                null,
                rule,
                null,
                null,
                null
        );

        return rule;
    }

    @Transactional
    public SavingsRule updateRule(UUID userId, UUID ruleId, UpdateRuleRequest request) {
        SavingsRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings Rule", ruleId));

        if (!rule.getUser().getId().equals(userId)) {
            throw new ValidationException("Rule does not belong to user");
        }

        if (request.name() != null) rule.setName(request.name());
        if (request.description() != null) rule.setDescription(request.description());
        if (request.ruleType() != null) rule.setRuleType(request.ruleType());
        if (request.percentage() != null) rule.setPercentage(request.percentage());
        if (request.fixedAmount() != null) rule.setFixedAmount(request.fixedAmount());
        if (request.minIncomeThreshold() != null) rule.setMinIncomeThreshold(request.minIncomeThreshold());
        if (request.maxIncomeThreshold() != null) rule.setMaxIncomeThreshold(request.maxIncomeThreshold());
        if (request.incomeSource() != null) rule.setIncomeSource(request.incomeSource());
        if (request.incomeSources() != null) rule.setIncomeSources(request.incomeSources());
        if (request.priority() != null) rule.setPriority(request.priority());
        if (request.isActive() != null) rule.setIsActive(request.isActive());

        validateRuleRequest(new CreateRuleRequest(
                rule.getVault().getId(),
                rule.getName(),
                rule.getDescription(),
                rule.getRuleType(),
                rule.getPercentage(),
                rule.getFixedAmount(),
                rule.getMinIncomeThreshold(),
                rule.getMaxIncomeThreshold(),
                rule.getIncomeSource(),
                rule.getIncomeSources(),
                rule.getPriority()
        ));

        rule = ruleRepository.save(rule);

        auditService.log(
                userId,
                userId,
                com.bikangu.common.enums.AuditAction.SAVINGS_RULE_UPDATED,
                "savings_rule",
                ruleId,
                null,
                rule,
                null,
                null,
                null
        );

        return rule;
    }

    @Transactional
    public void deleteRule(UUID userId, UUID ruleId) {
        SavingsRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings Rule", ruleId));

        if (!rule.getUser().getId().equals(userId)) {
            throw new ValidationException("Rule does not belong to user");
        }

        ruleRepository.delete(rule);

        auditService.log(
                userId,
                userId,
                com.bikangu.common.enums.AuditAction.SAVINGS_RULE_DELETED,
                "savings_rule",
                ruleId,
                rule,
                null,
                null,
                null,
                null
        );
    }

    @Transactional
    public SavingsRule pauseRule(UUID userId, UUID ruleId, String reason) {
        SavingsRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings Rule", ruleId));

        if (!rule.getUser().getId().equals(userId)) {
            throw new ValidationException("Rule does not belong to user");
        }

        rule.setIsPaused(true);
        rule.setPausedAt(Instant.now());
        rule.setPauseReason(reason);
        rule = ruleRepository.save(rule);

        auditService.log(
                userId,
                userId,
                com.bikangu.common.enums.AuditAction.SAVINGS_RULE_PAUSED,
                "savings_rule",
                ruleId,
                null,
                rule,
                null,
                null,
                null
        );

        return rule;
    }

    @Transactional
    public SavingsRule resumeRule(UUID userId, UUID ruleId) {
        SavingsRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings Rule", ruleId));

        if (!rule.getUser().getId().equals(userId)) {
            throw new ValidationException("Rule does not belong to user");
        }

        rule.setIsPaused(false);
        rule.setPausedAt(null);
        rule.setPauseReason(null);
        rule = ruleRepository.save(rule);

        auditService.log(
                userId,
                userId,
                com.bikangu.common.enums.AuditAction.SAVINGS_RULE_RESUMED,
                "savings_rule",
                ruleId,
                null,
                rule,
                null,
                null,
                null
        );

        return rule;
    }

    @Transactional(readOnly = true)
    public List<SavingsRule> getUserRules(UUID userId) {
        return ruleRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<SavingsRule> getActiveRules(UUID userId) {
        return ruleRepository.findByUserIdAndIsActiveTrueAndIsPausedFalse(userId);
    }

    private void validateRuleRequest(CreateRuleRequest request) {
        if (request.ruleType() == com.bikangu.common.enums.SavingsRuleType.PERCENTAGE ||
            request.ruleType() == com.bikangu.common.enums.SavingsRuleType.CONDITIONAL_PERCENTAGE ||
            request.ruleType() == com.bikangu.common.enums.SavingsRuleType.INCOME_SOURCE ||
            request.ruleType() == com.bikangu.common.enums.SavingsRuleType.BONUS) {
            if (request.percentage() == null || request.percentage().compareTo(BigDecimal.ZERO) <= 0 ||
                request.percentage().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new ValidationException("Percentage must be between 0 and 100");
            }
        }

        if (request.ruleType() == com.bikangu.common.enums.SavingsRuleType.FIXED_AMOUNT) {
            if (request.fixedAmount() == null || request.fixedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Fixed amount must be positive");
            }
        }
    }

    public record CreateRuleRequest(
            UUID vaultId,
            String name,
            String description,
            com.bikangu.common.enums.SavingsRuleType ruleType,
            BigDecimal percentage,
            BigDecimal fixedAmount,
            BigDecimal minIncomeThreshold,
            BigDecimal maxIncomeThreshold,
            String incomeSource,
            String[] incomeSources,
            Integer priority
    ) {}

    public record UpdateRuleRequest(
            String name,
            String description,
            com.bikangu.common.enums.SavingsRuleType ruleType,
            BigDecimal percentage,
            BigDecimal fixedAmount,
            BigDecimal minIncomeThreshold,
            BigDecimal maxIncomeThreshold,
            String incomeSource,
            String[] incomeSources,
            Integer priority,
            Boolean isActive
    ) {}
}