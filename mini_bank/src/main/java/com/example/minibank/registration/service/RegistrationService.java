package com.example.minibank.registration.service;

import com.example.minibank.account.entity.Account;
import com.example.minibank.account.entity.AccountBalance;
import com.example.minibank.account.service.AccountService;
import com.example.minibank.auth.entity.User;
import com.example.minibank.auth.service.AuthService;
import com.example.minibank.customer.entity.Customer;
import com.example.minibank.customer.service.CustomerService;
import com.example.minibank.registration.dto.RegistrationRequest;
import com.example.minibank.registration.dto.RegistrationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final AuthService authService;
    private final CustomerService customerService;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {

        // 1. Create user
        User user = authService.createUser(
                request.username(),
                request.email(),
                request.password()
        );

        // 2. Create customer
        Customer customer = customerService.createCustomer(
                user,
                request.fullName(),
                request.phone(),
                request.address()
        );

        // 3. Open bank account
        Account account = accountService.openSavingsAccount(customer);

        // 4. Initialize balance
        AccountBalance balance =
                accountService.initializeBalance(account);

        // 5. Build response
        return RegistrationResponse.from(
                customer,
                account,
                balance
        );
    }
}