package com.example.minibank.account.service;
import com.example.minibank.account.entity.Account;
import com.example.minibank.account.entity.AccountBalance;
import com.example.minibank.account.repository.AccountBalanceRepository;
import com.example.minibank.account.repository.AccountRepository;
import com.example.minibank.common.enums.AccountStatus;
import com.example.minibank.common.enums.AccountType;
import com.example.minibank.customer.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountBalanceRepository balanceRepository;

    private final SecureRandom random = new SecureRandom();

    public Account openSavingsAccount(Customer customer) {

        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(AccountType.SAVINGS);
        account.setCurrency("IDR");
        account.setStatus(AccountStatus.ACTIVE);

        return accountRepository.save(account);
    }

    public AccountBalance initializeBalance(Account account) {

        AccountBalance balance = new AccountBalance();
        balance.setAccount(account);
        balance.setAvailableBalance(BigDecimal.ZERO);
        balance.setLedgerBalance(BigDecimal.ZERO);

        return balanceRepository.save(balance);
    }

    private String generateAccountNumber() {

        String accountNumber;

        do {
            accountNumber = String.format(
                    "%012d",
                    random.nextLong(1_000_000_000_000L)
            );
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}
