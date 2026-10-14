package com.bikangu.providers.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProviderAccount(
        UUID id,
        String providerAccountId,
        String accountType,
        String accountName,
        String accountNumberMasked,
        String accountHolderName,
        String currency,
        BigDecimal currentBalance,
        BigDecimal availableBalance,
        boolean isVerified,
        boolean isActive,
        Instant lastSyncAt,
        List<ProviderTransaction> recentTransactions
) {}