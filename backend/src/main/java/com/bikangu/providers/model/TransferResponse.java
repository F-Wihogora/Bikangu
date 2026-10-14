package com.bikangu.providers.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID id,
        String externalTransferId,
        String status,
        BigDecimal amount,
        String currency,
        BigDecimal feeAmount,
        BigDecimal netAmount,
        Instant initiatedAt,
        Instant completedAt,
        String failureReason
) {}