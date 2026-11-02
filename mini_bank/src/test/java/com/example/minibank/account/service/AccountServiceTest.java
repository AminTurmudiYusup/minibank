package com.example.minibank.account.service;

import com.example.minibank.account.entity.Account;
import com.example.minibank.account.entity.AccountBalance;
import com.example.minibank.account.repository.AccountBalanceRepository;
import com.example.minibank.account.repository.AccountRepository;
import com.example.minibank.common.enums.AccountStatus;
import com.example.minibank.common.enums.AccountType;
import com.example.minibank.customer.entity.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountBalanceRepository balanceRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldOpenSavingsAccountSuccessfully() {

        // Arrange
        Customer customer = new Customer();

        when(accountRepository.existsByAccountNumber(anyString()))
                .thenReturn(false);

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Account result = accountService.openSavingsAccount(customer);

        // Assert
        assertNotNull(result);
        assertEquals(customer, result.getCustomer());
        assertEquals(AccountType.SAVINGS, result.getAccountType());
        assertEquals("IDR", result.getCurrency());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());

        assertNotNull(result.getAccountNumber());
        assertTrue(result.getAccountNumber().matches("\\d{12}"));

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldInitializeAccountBalanceSuccessfully() {

        // Arrange
        Account account = new Account();

        when(balanceRepository.save(any(AccountBalance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccountBalance result =
                accountService.initializeBalance(account);

        // Assert
        assertNotNull(result);
        assertEquals(account, result.getAccount());
        assertEquals(
                BigDecimal.ZERO,
                result.getAvailableBalance()
        );
        assertEquals(
                BigDecimal.ZERO,
                result.getLedgerBalance()
        );

        verify(balanceRepository).save(any(AccountBalance.class));
    }
}