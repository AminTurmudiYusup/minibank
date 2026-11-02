package com.example.minibank.transaction.dto;


import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TransferRequest(

        @NotBlank
        @Size(min = 12, max = 30)
        String toAccountNumber,

        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount,

        @Size(max = 255)
        String description

) {
}

