package com.example.minibank.transaction.service;

import com.example.minibank.account.entity.Account;
import com.example.minibank.account.entity.AccountBalance;
import com.example.minibank.account.repository.AccountBalanceRepository;
import com.example.minibank.account.repository.AccountRepository;
import com.example.minibank.common.enums.AccountStatus;
import com.example.minibank.common.enums.TransferEntryType;
import com.example.minibank.common.enums.TransferStatus;
import com.example.minibank.common.exception.BusinessException;
import com.example.minibank.common.exception.ResourceNotFoundException;
import com.example.minibank.customer.entity.Customer;
import com.example.minibank.customer.repository.CustomerRepository;
import com.example.minibank.transaction.dto.TransferRequest;
import com.example.minibank.transaction.dto.TransferResponse;
import com.example.minibank.transaction.entity.IdempotencyRecord;
import com.example.minibank.transaction.entity.Transfer;
import com.example.minibank.transaction.entity.TransferEntry;
import com.example.minibank.transaction.repository.TransferEntryRepository;
import com.example.minibank.transaction.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final AccountBalanceRepository accountBalanceRepository;
    private final TransferRepository transferRepository;
    private final TransferEntryRepository transferEntryRepository;
    private final IdempotencyService idempotencyService;
    private final CustomerRepository customerRepository;

    @Transactional
    public TransferResponse transfer(
            UUID userId, String idempotencyKey,
            TransferRequest request) {


        // 1. Check existing request
        Optional<IdempotencyRecord> existing =
                idempotencyService.findExisting(userId, idempotencyKey);

        if (existing.isPresent()) {
            Transfer transfer = transferRepository
                    .findById(existing.get().getTransferId())
                    .orElseThrow();

            return toResponse(transfer);
        }


        // 2. Validate amount
        BigDecimal amount = request.amount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Transfer amount must be greater than zero");
        }

        // 3. Find authenticated customer's profile.
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found")
                );

        // 4. Find sender account
        List<Account> senderAccounts =
                accountRepository.findByCustomerId(customer.getId());

        if (senderAccounts.size() != 1) {
            throw new BusinessException(
                    "Expected exactly one account for this customer");
        }

        Account sender = senderAccounts.getFirst();

        // 5. Find receiver account by account number
        Account receiver = accountRepository
                .findByAccountNumber(request.toAccountNumber())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Receiver account not found"));

        // 6. Validate accounts
        if (sender.getId().equals(receiver.getId())) {
            throw new BusinessException(
                    "Cannot transfer to the same account");
        }

        if (sender.getStatus() != AccountStatus.ACTIVE
                || receiver.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Both accounts must be active");
        }

        if (!sender.getCurrency().equals(receiver.getCurrency())) {
            throw new BusinessException("Account currencies do not match");
        }

        // 7. Lock both balances in deterministic order
        List<UUID> accountIds = List.of(
                sender.getId(),
                receiver.getId()
        ).stream().sorted().toList();

        Map<UUID, AccountBalance> balances = accountIds.stream()
                .map(accountId -> accountBalanceRepository
                        .findByAccountIdForUpdate(accountId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Account balance not found")))
                .collect(Collectors.toMap(
                        balance -> balance.getAccount().getId(),
                        Function.identity()
                ));

        AccountBalance senderBalance = balances.get(sender.getId());
        AccountBalance receiverBalance = balances.get(receiver.getId());

        // 8. Check sufficient balance
        if (senderBalance.getAvailableBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance");
        }

        // 9. Update balances
        senderBalance.setAvailableBalance(
                senderBalance.getAvailableBalance().subtract(amount)
        );

        senderBalance.setLedgerBalance(
                senderBalance.getLedgerBalance().subtract(amount)
        );

        receiverBalance.setAvailableBalance(
                receiverBalance.getAvailableBalance().add(amount)
        );

        receiverBalance.setLedgerBalance(
                receiverBalance.getLedgerBalance().add(amount)
        );
        accountBalanceRepository.save(senderBalance);
        accountBalanceRepository.save(receiverBalance);

        // 10. Create transfer record
        Transfer transfer = new Transfer();
        transfer.setReferenceNumber(
                "TRF-" + UUID.randomUUID());
        transfer.setFromAccount(sender);
        transfer.setToAccount(receiver);
        transfer.setAmount(amount);
        transfer.setCurrency(sender.getCurrency());
        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setDescription(request.description());

        Transfer savedTransfer = transferRepository.save(transfer);

        // 11. Create debit entry
        TransferEntry debitEntry = new TransferEntry();
        debitEntry.setTransfer(savedTransfer);
        debitEntry.setAccount(sender);
        debitEntry.setEntryType(TransferEntryType.DEBIT);
        debitEntry.setAmount(amount);

        // 12. Create credit entry
        TransferEntry creditEntry = new TransferEntry();
        creditEntry.setTransfer(savedTransfer);
        creditEntry.setAccount(receiver);
        creditEntry.setEntryType(TransferEntryType.CREDIT);
        creditEntry.setAmount(amount);

        transferEntryRepository.saveAll(
                List.of(debitEntry, creditEntry));

        // 13. save idempotency
        idempotencyService.save(
                userId,
                idempotencyKey,
                savedTransfer.getId()
        );


        // 14. Return response
        return toResponse(savedTransfer);
    }

    private TransferResponse toResponse(Transfer transfer) {

        return new TransferResponse(
                transfer.getId(),
                transfer.getReferenceNumber(),
                transfer.getFromAccount().getAccountNumber(),
                transfer.getToAccount().getAccountNumber(),
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getStatus().name(),
                transfer.getCreatedAt()
        );
    }
}

