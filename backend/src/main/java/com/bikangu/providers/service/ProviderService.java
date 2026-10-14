package com.bikangu.providers.service;

import com.bikangu.common.exception.ProviderException;
import com.bikangu.common.exception.ResourceNotFoundException;
import com.bikangu.providers.FinancialProvider;
import com.bikangu.providers.ProviderRegistry;
import com.bikangu.providers.entity.FinancialProviderEntity;
import com.bikangu.providers.model.ProviderAccount;
import com.bikangu.providers.model.ProviderTransaction;
import com.bikangu.providers.model.TransferRequest;
import com.bikangu.providers.model.TransferResponse;
import com.bikangu.providers.repository.FinancialProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderService {

    private final ProviderRegistry providerRegistry;
    private final FinancialProviderRepository providerRepository;

    @Transactional(readOnly = true)
    public List<FinancialProviderEntity> getAllProviders() {
        return providerRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<FinancialProviderEntity> getProvidersByCountry(String countryCode) {
        return providerRepository.findByCountryCode(countryCode);
    }

    @Transactional(readOnly = true)
    public FinancialProviderEntity getProviderById(UUID id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial Provider", id));
    }

    @Transactional(readOnly = true)
    public FinancialProvider getProviderImplementation(String providerId) {
        return providerRegistry.getProvider(providerId);
    }

    @Transactional
    public ProviderAccount verifyAccount(UUID providerId, String accountId, String credentials) {
        FinancialProvider provider = providerRegistry.getProvider(providerId.toString());
        try {
            return provider.verifyAccount(accountId, credentials);
        } catch (Exception e) {
            log.error("Account verification failed for provider {}: {}", providerId, e.getMessage());
            throw new ProviderException("Account verification failed", providerId.toString(), e.getMessage());
        }
    }

    @Transactional
    public ProviderAccount getBalance(UUID providerId, String accountId) {
        FinancialProvider provider = providerRegistry.getProvider(providerId.toString());
        try {
            return provider.getBalance(accountId);
        } catch (Exception e) {
            log.error("Balance check failed for provider {}: {}", providerId, e.getMessage());
            throw new ProviderException("Balance check failed", providerId.toString(), e.getMessage());
        }
    }

    @Transactional
    public List<ProviderTransaction> getTransactions(UUID providerId, String accountId, int limit, String cursor) {
        FinancialProvider provider = providerRegistry.getProvider(providerId.toString());
        try {
            return provider.getTransactions(accountId, limit, cursor);
        } catch (Exception e) {
            log.error("Transaction history failed for provider {}: {}", providerId, e.getMessage());
            throw new ProviderException("Transaction history failed", providerId.toString(), e.getMessage());
        }
    }

    @Transactional
    public List<ProviderTransaction> detectIncome(UUID providerId, String accountId, Instant since) {
        FinancialProvider provider = providerRegistry.getProvider(providerId.toString());
        try {
            return provider.detectIncome(accountId, since);
        } catch (Exception e) {
            log.error("Income detection failed for provider {}: {}", providerId, e.getMessage());
            throw new ProviderException("Income detection failed", providerId.toString(), e.getMessage());
        }
    }

    @Transactional
    public TransferResponse initiateTransfer(UUID providerId, TransferRequest request) {
        FinancialProvider provider = providerRegistry.getProvider(providerId.toString());
        try {
            return provider.initiateTransfer(request);
        } catch (Exception e) {
            log.error("Transfer initiation failed for provider {}: {}", providerId, e.getMessage());
            throw new ProviderException("Transfer initiation failed", providerId.toString(), e.getMessage());
        }
    }

    @Transactional
    public TransferResponse getTransferStatus(UUID providerId, String externalTransferId) {
        FinancialProvider provider = providerRegistry.getProvider(providerId.toString());
        try {
            return provider.getTransferStatus(externalTransferId);
        } catch (Exception e) {
            log.error("Transfer status check failed for provider {}: {}", providerId, e.getMessage());
            throw new ProviderException("Transfer status check failed", providerId.toString(), e.getMessage());
        }
    }

    @Transactional
    public void handleWebhook(UUID providerId, String payload, String signature) {
        FinancialProvider provider = providerRegistry.getProvider(providerId.toString());
        try {
            provider.handleWebhook(payload, signature);
        } catch (Exception e) {
            log.error("Webhook handling failed for provider {}: {}", providerId, e.getMessage());
            throw new ProviderException("Webhook handling failed", providerId.toString(), e.getMessage());
        }
    }

    @Transactional
    public boolean checkProviderHealth(UUID providerId) {
        FinancialProvider provider = providerRegistry.getProvider(providerId.toString());
        return provider.isHealthy();
    }
}