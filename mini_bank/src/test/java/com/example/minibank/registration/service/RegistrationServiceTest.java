package com.example.minibank.registration.service;

import com.example.minibank.account.entity.Account;
import com.example.minibank.account.entity.AccountBalance;
import com.example.minibank.account.service.AccountService;
import com.example.minibank.auth.entity.User;
import com.example.minibank.auth.service.AuthService;
import com.example.minibank.common.enums.AccountStatus;
import com.example.minibank.common.enums.AccountType;
import com.example.minibank.customer.entity.Customer;
import com.example.minibank.customer.service.CustomerService;
import com.example.minibank.registration.dto.RegistrationRequest;
import com.example.minibank.registration.dto.RegistrationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private AuthService authService;

    @Mock
    private CustomerService customerService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void shouldRegisterCustomerSuccessfully() {

        // Arrange
        RegistrationRequest request = new RegistrationRequest(
                "john",
                "john@gmail.com",
                "Password123!",
                "John Doe",
                "08123456789",
                "Jakarta"
        );

        User user = new User();
        user.setUsername("john");

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setFullName("John Doe");
        customer.setPhone("08123456789");

        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountNumber("100000000001");
        account.setAccountType(AccountType.SAVINGS);
        account.setCurrency("IDR");
        account.setStatus(AccountStatus.ACTIVE);

        AccountBalance balance = new AccountBalance();
        balance.setAccount(account);
        balance.setAvailableBalance(BigDecimal.ZERO);
        balance.setLedgerBalance(BigDecimal.ZERO);

        when(authService.createUser(
                "john",
                "john@gmail.com",
                "Password123!"
        )).thenReturn(user);

        when(customerService.createCustomer(
                user,
                "John Doe",
                "08123456789",
                "Jakarta"
        )).thenReturn(customer);

        when(accountService.openSavingsAccount(customer))
                .thenReturn(account);

        when(accountService.initializeBalance(account))
                .thenReturn(balance);

        // Act
        RegistrationResponse result =
                registrationService.register(request);

        // Assert
        assertNotNull(result);

        assertEquals(
                "Registration successful",
                result.message()
        );

        assertEquals(
                "John Doe",
                result.customer().fullName()
        );

        assertEquals(
                "100000000001",
                result.account().accountNumber()
        );

        assertEquals(
                BigDecimal.ZERO,
                result.account().balance()
        );

        // Verify execution order
        InOrder inOrder = inOrder(
                authService,
                customerService,
                accountService
        );

        inOrder.verify(authService).createUser(
                "john",
                "john@gmail.com",
                "Password123!"
        );

        inOrder.verify(customerService).createCustomer(
                user,
                "John Doe",
                "08123456789",
                "Jakarta"
        );

        inOrder.verify(accountService).openSavingsAccount(customer);

        inOrder.verify(accountService).initializeBalance(account);
    }
}