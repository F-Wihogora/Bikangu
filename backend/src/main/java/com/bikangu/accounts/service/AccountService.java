package com.bikangu.accounts.service;

import com.bikangu.audit.service.AuditService;
import com.bikangu.accounts.entity.FinancialAccount;
import com.bikangu.accounts.repository.FinancialAccountRepository;
import com.bikangu.authentication.entity.User;
import com.bikangu.authentication.repository.UserRepository;
import com.bikangu.common.exception.ConflictException;
import com.bikangu.common.exception.ResourceNotFoundException;
import com.bikangu.common.exception.ValidationException;
import com.bikangu.providers.entity.FinancialProviderEntity;
import com.bikangu.providers.model.ProviderAccount;
import com.bikangu.providers.repository.FinancialProviderRepository;
import com.bikangu.providers.service.ProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final FinancialAccountRepository accountRepository;
    private final FinancialProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final ProviderService providerService;
    private final AuditService auditService;

    @Transactional
    public FinancialAccount linkAccount(UUID userId, LinkAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        FinancialProviderEntity provider = providerRepository.findById(request.providerId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider", request.providerId()));

        // Check if account already linked
        Optional<FinancialAccount> existing = accountRepository.findByUserProviderAndAccountId(
                userId, request.providerId(), request.providerAccountId());
        if (existing.isPresent()) {
            throw new ConflictException("This account is already linked");
        }

        // Verify account with provider
        ProviderAccount providerAccount = providerService.verifyAccount(
                request.providerId(), request.providerAccountId(), request.credentials());

        FinancialAccount account = FinancialAccount.builder()
                .user(user)
                .provider(provider)
                .providerAccountId(request.providerAccountId())
                .providerAccountType(providerAccount.accountType())
                .accountName(request.accountName() != null ? request.accountName() : providerAccount.accountName())
                .accountNumberMasked(providerAccount.accountNumberMasked())
                .accountHolderName(providerAccount.accountHolderName())
                .currency(providerAccount.currency())
                .isPrimary(request.isPrimary())
                .isVerified(providerAccount.isVerified())
                .isActive(true)
                .currentBalance(providerAccount.currentBalance())
                .availableBalance(providerAccount.availableBalance())
                .verificationStatus(com.bikangu.common.enums.AccountVerificationStatus.VERIFIED)
                .verifiedAt(Instant.now())
                .lastSyncAt(Instant.now())
                .lastBalanceCheckAt(Instant.now())
                .build();

        // If this is primary, unset other primary accounts
        if (request.isPrimary()) {
            accountRepository.findByUserIdAndIsPrimaryTrue(userId).ifPresent(a -> {
                a.setIsPrimary(false);
                accountRepository.save(a);
            });
        }

        account = accountRepository.save(account);

        auditService.log(
                userId,
                userId,
                com.bikangu.common.enums.AuditAction.ACCOUNT_LINKED,
                "financial_account",
                account.getId(),
                null,
                account,
                null,
                null,
                null
        );

        return account;
    }

    @Transactional
    public FinancialAccount updateAccount(UUID userId, UUID accountId, UpdateAccountRequest request) {
        FinancialAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial Account", accountId));

        if (!account.getUser().getId().equals(userId)) {
            throw new ValidationException("Account does not belong to user");
        }

        if (request.accountName() != null) {
            account.setAccountName(request.accountName());
        }
        if (request.isPrimary() != null && request.isPrimary() && !account.getIsPrimary()) {
            // Unset other primary accounts
            accountRepository.findByUserIdAndIsPrimaryTrue(userId).ifPresent(a -> {
                a.setIsPrimary(false);
                accountRepository.save(a);
            });
            account.setIsPrimary(true);
        }
        if (request.isActive() != null) {
            account.setIsActive(request.isActive());
        }

        account = accountRepository.save(account);

        auditService.log(
                userId,
                userId,
                com.bikangu.common.enums.AuditAction.ACCOUNT_SET_PRIMARY,
                "financial_account",
                accountId,
                null,
                account,
                null,
                null,
                null
        );

        return account;
    }

    @Transactional
    public void unlinkAccount(UUID userId, UUID accountId) {
        FinancialAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial Account", accountId));

        if (!account.getUser().getId().equals(userId)) {
            throw new ValidationException("Account does not belong to user");
        }

        account.setIsActive(false);
        accountRepository.save(account);

        auditService.log(
                userId,
                userId,
                com.bikangu.common.enums.AuditAction.ACCOUNT_UNLINKED,
                "financial_account",
                accountId,
                account,
                null,
                null,
                null,
                null
        );
    }

    @Transactional(readOnly = true)
    public List<FinancialAccount> getUserAccounts(UUID userId) {
        return accountRepository.findByUserIdAndIsActiveTrue(userId);
    }

    @Transactional(readOnly = true)
    public Optional<FinancialAccount> getPrimaryAccount(UUID userId) {
        return accountRepository.findByUserIdAndIsPrimaryTrue(userId);
    }

    @Transactional
    public FinancialAccount syncBalance(UUID userId, UUID accountId) {
        FinancialAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial Account", accountId));

        if (!account.getUser().getId().equals(userId)) {
            throw new ValidationException("Account does not belong to user");
        }

        ProviderAccount providerAccount = providerService.getBalance(account.getProvider().getId(), account.getProviderAccountId());

        account.setCurrentBalance(providerAccount.currentBalance());
        account.setAvailableBalance(providerAccount.availableBalance());
        account.setLastBalanceCheckAt(Instant.now());
        account.setLastSyncAt(Instant.now());

        return accountRepository.save(account);
    }

    @Transactional
    public FinancialAccount refreshAccount(UUID userId, UUID accountId) {
        FinancialAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial Account", accountId));

        if (!account.getUser().getId().equals(userId)) {
            throw new ValidationException("Account does not belong to user");
        }

        ProviderAccount providerAccount = providerService.verifyAccount(
                account.getProvider().getId(), account.getProviderAccountId(), "");

        account.setCurrentBalance(providerAccount.currentBalance());
        account.setAvailableBalance(providerAccount.availableBalance());
        account.setIsVerified(providerAccount.isVerified());
        account.setLastSyncAt(Instant.now());
        account.setLastBalanceCheckAt(Instant.now());

        return accountRepository.save(account);
    }

    public record LinkAccountRequest(
            UUID providerId,
            String providerAccountId,
            String credentials,
            String accountName,
            boolean isPrimary
    ) {}

    public record UpdateAccountRequest(
            String accountName,
            Boolean isPrimary,
            Boolean isActive
    ) {}
}