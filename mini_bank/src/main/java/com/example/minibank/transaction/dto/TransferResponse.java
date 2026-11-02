package com.example.minibank.transaction.dto;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID transferId,
        String referenceNumber,
        String fromAccountNumber,
        String toAccountNumber,
        BigDecimal amount,
        String currency,
        String status,
        Instant createdAt
) {
}

