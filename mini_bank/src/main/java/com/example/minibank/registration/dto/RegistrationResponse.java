package com.example.minibank.registration.dto;

import com.example.minibank.account.entity.Account;
import com.example.minibank.account.entity.AccountBalance;
import com.example.minibank.customer.entity.Customer;

import java.math.BigDecimal;
import java.util.UUID;

public record RegistrationResponse(
        String message,
        CustomerInfo customer,
        AccountInfo account
) {

    public record CustomerInfo(
            UUID id,
            String fullName,
            String email,
            String phone
    ) {}

    public record AccountInfo(
            UUID id,
            String accountNumber,
            String accountType,
            String currency,
            BigDecimal balance,
            String status
    ) {}

    public static RegistrationResponse from(
            Customer customer,
            Account account,
            AccountBalance balance
    ) {
        return new RegistrationResponse(
                "Registration successful",

                new CustomerInfo(
                        customer.getId(),
                        customer.getFullName(),
                        customer.getUser().getEmail(),
                        customer.getPhone()
                ),

                new AccountInfo(
                        account.getId(),
                        account.getAccountNumber(),
                        account.getAccountType().name(),
                        account.getCurrency(),
                        balance.getAvailableBalance(),
                        account.getStatus().name()
                )
        );
    }
}