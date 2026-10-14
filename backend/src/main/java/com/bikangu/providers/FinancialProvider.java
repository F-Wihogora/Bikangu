package com.bikangu.providers;

import com.bikangu.providers.model.ProviderAccount;
import com.bikangu.providers.model.ProviderTransaction;
import com.bikangu.providers.model.TransferRequest;
import com.bikangu.providers.model.TransferResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FinancialProvider {

    String getProviderId();

    String getProviderName();

    boolean isSandbox();

    // Account verification
    ProviderAccount verifyAccount(String accountId, String credentials);

    // Balance inquiry
    ProviderAccount getBalance(String accountId);

    // Transaction history
    List<ProviderTransaction> getTransactions(String accountId, int limit, String cursor);

    // Income detection
    List<ProviderTransaction> detectIncome(String accountId, Instant since);

    // Transfer initiation
    TransferResponse initiateTransfer(TransferRequest request);

    // Transfer status
    TransferResponse getTransferStatus(String externalTransferId);

    // Webhook handling
    void handleWebhook(String payload, String signature);

    // Health check
    boolean isHealthy();
}