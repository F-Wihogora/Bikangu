package com.bikangu.savings.service;

import com.bikangu.audit.service.AuditService;
import com.bikangu.common.exception.ResourceNotFoundException;
import com.bikangu.common.exception.ValidationException;
import com.bikangu.savings.entity.SavingsVault;
import com.bikangu.savings.repository.SavingsVaultRepository;
import com.bikangu.authentication.entity.User;
import com.bikangu.authentication.repository.UserRepository;
import com.bikangu.goals.entity.SavingsGoal;
import com.bikangu.goals.repository.SavingsGoalRepository;
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
public class SavingsVaultService {

    private final SavingsVaultRepository vaultRepository;
    private final SavingsGoalRepository goalRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public SavingsVault createVault(UUID userId, CreateVaultRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        SavingsVault vault = SavingsVault.builder()
                .user(user)
                .name(request.name())
                .description(request.description())
                .targetAmount(request.targetAmount())
                .currency(request.currency())
                .icon(request.icon())
                .color(request.color())
                .startDate(request.startDate() != null ? request.startDate() : LocalDate.now())
                .lockDate(request.lockDate())
                .unlockDate(request.unlockDate())
                .interestRate(request.interestRate() != null ? request.interestRate() : BigDecimal.ZERO)
                .autoLock(request.autoLock() != null ? request.autoLock() : true)
                .allowEarlyWithdrawal(request.allowEarlyWithdrawal() != null ? request.allowEarlyWithdrawal() : false)
                .earlyWithdrawalPenaltyRate(request.earlyWithdrawalPenaltyRate() != null ? request.earlyWithdrawalPenaltyRate() : BigDecimal.ZERO)
                .minimumBalance(request.minimumBalance() != null ? request.minimumBalance() : BigDecimal.ZERO)
                .build();

        vault = vaultRepository.save(vault);

        auditService.log(
                userId,
                userId,
                com.bikangu.common.enums.AuditAction.SAVINGS_VAULT_CREATED,
                "savings_vault",
                vault.getId(),
                null,
                vault,
                null,
                null,
                null
        );

        return vault;
    }

    @Transactional
    public SavingsVault updateVault(UUID userId, UUID vaultId, UpdateVaultRequest request) {
        SavingsVault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings Vault", vaultId));

        if (!vault.getUser().getId().equals(userId)) {
            throw new ValidationException("Vault does not belong to user");
        }

        if (request.name() != null) vault.setName(request.name());
        if (request.description() != null) vault.setDescription(request.description());
        if (request.targetAmount() != null) vault.setTargetAmount(request.targetAmount());
        if (request.icon() != null) vault.setIcon(request.icon());
        if (request.color() != null) vault.setColor(request.color());
        if (request.lockDate() != null) vault.setLockDate(request.lockDate());
        if (request.unlockDate() != null) vault.setUnlockDate(request.unlockDate());
        if (request.interestRate() != null) vault.setInterestRate(request.interestRate());
        if (request.autoLock() != null) vault.setAutoLock(request.autoLock());
        if (request.allowEarlyWithdrawal() != null) vault.setAllowEarlyWithdrawal(request.allowEarlyWithdrawal());
        if (request.earlyWithdrawalPenaltyRate() != null) vault.setEarlyWithdrawalPenaltyRate(request.earlyWithdrawalPenaltyRate());
        if (request.minimumBalance() != null) vault.setMinimumBalance(request.minimumBalance());

        vault = vaultRepository.save(vault);

        auditService.log(
                userId,
                userId,
                com.bikangu.common.enums.AuditAction.SAVINGS_VAULT_UPDATED,
                "savings_vault",
                vaultId,
                null,
                vault,
                null,
                null,
                null
        );

        return vault;
    }

    @Transactional
    public void closeVault(UUID userId, UUID vaultId) {
        SavingsVault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings Vault", vaultId));

        if (!vault.getUser().getId().equals(userId)) {
            throw new ValidationException("Vault does not belong to user");
        }

        if (vault.getCurrentAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new ValidationException("Cannot close vault with remaining balance. Withdraw funds first.");
        }

        vault.setStatus(com.bikangu.common.enums.VaultStatus.CLOSED);
        vaultRepository.save(vault);

        auditService.log(
                userId,
                userId,
                com.bikangu.common.enums.AuditAction.SAVINGS_VAULT_CLOSED,
                "savings_vault",
                vaultId,
                vault,
                null,
                null,
                null,
                null
        );
    }

    @Transactional(readOnly = true)
    public List<SavingsVault> getUserVaults(UUID userId) {
        return vaultRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<SavingsVault> getActiveVaults(UUID userId) {
        return vaultRepository.findByUserIdAndStatusIn(userId,
                List.of(com.bikangu.common.enums.VaultStatus.ACTIVE, com.bikangu.common.enums.VaultStatus.LOCKED));
    }

    @Transactional(readOnly = true)
    public SavingsVault getVault(UUID userId, UUID vaultId) {
        SavingsVault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings Vault", vaultId));

        if (!vault.getUser().getId().equals(userId)) {
            throw new ValidationException("Vault does not belong to user");
        }

        return vault;
    }

    @Transactional(readOnly = true)
    public List<SavingsVault> getLockedVaults(UUID userId) {
        return vaultRepository.findLockedVaultsWithUnlockDate(userId, LocalDate.now());
    }

    public record CreateVaultRequest(
            String name,
            String description,
            BigDecimal targetAmount,
            String currency,
            String icon,
            String color,
            LocalDate startDate,
            LocalDate lockDate,
            LocalDate unlockDate,
            BigDecimal interestRate,
            Boolean autoLock,
            Boolean allowEarlyWithdrawal,
            BigDecimal earlyWithdrawalPenaltyRate,
            BigDecimal minimumBalance
    ) {}

    public record UpdateVaultRequest(
            String name,
            String description,
            BigDecimal targetAmount,
            String icon,
            String color,
            LocalDate lockDate,
            LocalDate unlockDate,
            BigDecimal interestRate,
            Boolean autoLock,
            Boolean allowEarlyWithdrawal,
            BigDecimal earlyWithdrawalPenaltyRate,
            BigDecimal minimumBalance
    ) {}
}