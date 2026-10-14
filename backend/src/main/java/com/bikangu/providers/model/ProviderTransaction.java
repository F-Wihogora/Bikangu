package com.bikangu.providers.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProviderTransaction(
        UUID id,
        String externalTransactionId,
        String type,
        String category,
        BigDecimal amount,
        String currency,
        String description,
        String reference,
        Instant transactionDate,
        String status,
        String incomeSource,
        String incomeCategory
) {}