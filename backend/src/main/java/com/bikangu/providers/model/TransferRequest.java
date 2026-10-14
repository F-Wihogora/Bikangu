package com.bikangu.providers.model;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        UUID idempotencyKey,
        String fromAccountId,
        String toAccountId,
        BigDecimal amount,
        String currency,
        String description,
        String reference
) {}