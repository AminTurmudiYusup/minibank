package com.example.minibank.transaction.service;

import com.example.minibank.account.entity.Account;
import com.example.minibank.account.entity.AccountBalance;
import com.example.minibank.account.repository.AccountBalanceRepository;
import com.example.minibank.account.repository.AccountRepository;
import com.example.minibank.common.enums.AccountStatus;
import com.example.minibank.common.enums.TransferStatus;
import com.example.minibank.common.exception.BusinessException;
import com.example.minibank.common.exception.ResourceNotFoundException;
import com.example.minibank.customer.entity.Customer;
import com.example.minibank.customer.repository.CustomerRepository;
import com.example.minibank.transaction.dto.TransferRequest;
import com.example.minibank.transaction.dto.TransferResponse;
import com.example.minibank.transaction.entity.IdempotencyRecord;
import com.example.minibank.transaction.entity.Transfer;
import com.example.minibank.transaction.repository.TransferEntryRepository;
import com.example.minibank.transaction.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final String IDEMPOTENCY_KEY = "request-001";
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountBalanceRepository accountBalanceRepository;
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private TransferEntryRepository transferEntryRepository;
    @Mock
    private IdempotencyService idempotencyService;
    @Mock
    private CustomerRepository customerRepository;
    @InjectMocks
    private TransferService transferService;
    private UUID userId;
    private UUID customerId;
    private UUID senderId;
    private UUID receiverId;
    private Customer customer;
    private Account sender;
    private Account receiver;
    private AccountBalance senderBalance;
    private AccountBalance receiverBalance;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();

        // Customer
        customer = new Customer();
        customer.setId(customerId);

        // Sender account
        sender = new Account();
        sender.setId(senderId);
        sender.setAccountNumber("111111111111");
        sender.setCustomer(customer);
        sender.setCurrency("IDR");
        sender.setStatus(AccountStatus.ACTIVE);

        // Receiver account
        receiver = new Account();
        receiver.setId(receiverId);
        receiver.setAccountNumber("222222222222");
        receiver.setCurrency("IDR");
        receiver.setStatus(AccountStatus.ACTIVE);

        // Sender balance
        senderBalance = new AccountBalance();
        senderBalance.setAccount(sender);
        senderBalance.setAvailableBalance(new BigDecimal("500000"));
        senderBalance.setLedgerBalance(new BigDecimal("500000"));

        // Receiver balance
        receiverBalance = new AccountBalance();
        receiverBalance.setAccount(receiver);
        receiverBalance.setAvailableBalance(new BigDecimal("100000"));
        receiverBalance.setLedgerBalance(new BigDecimal("100000"));
    }

    // ==================================================
    // TEST SETUP HELPERS
    // ==================================================

    private void mockNoExistingRequest() {

        when(idempotencyService.findExisting(userId, IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());
    }

    private void mockCustomerAndSender() {

        when(customerRepository.findByUserId(userId))
                .thenReturn(Optional.of(customer));

        when(accountRepository.findByCustomerId(customerId))
                .thenReturn(List.of(sender));
    }

    private void mockReceiver() {

        when(accountRepository.findByAccountNumber(receiver.getAccountNumber()))
                .thenReturn(Optional.of(receiver));
    }

    private void mockBalances() {

        when(accountBalanceRepository.findByAccountIdForUpdate(senderId))
                .thenReturn(Optional.of(senderBalance));

        when(accountBalanceRepository.findByAccountIdForUpdate(receiverId))
                .thenReturn(Optional.of(receiverBalance));
    }

    private TransferRequest validRequest() {

        return new TransferRequest(
                receiver.getAccountNumber(),
                new BigDecimal("100000"),
                "Test transfer"
        );
    }

    // ==================================================
    // 1. SUCCESSFUL TRANSFER
    // ==================================================

    @Test
    void shouldTransferSuccessfully() {

        // Arrange
        mockNoExistingRequest();
        mockCustomerAndSender();
        mockReceiver();
        mockBalances();
        when(transferRepository.save(any(Transfer.class)))
                .thenAnswer(invocation -> {
                    Transfer transfer = invocation.getArgument(0);
                    transfer.setId(UUID.randomUUID());
                    return transfer;
                });
        TransferRequest request = validRequest();

        // Initial sender balance
        senderBalance.setAvailableBalance(
                new BigDecimal("500000")
        );
        senderBalance.setLedgerBalance(
                new BigDecimal("500000")
        );

        // Initial receiver balance
        receiverBalance.setAvailableBalance(
                new BigDecimal("200000")
        );
        receiverBalance.setLedgerBalance(
                new BigDecimal("200000")
        );

        // Act
        TransferResponse response = transferService.transfer(
                userId,
                IDEMPOTENCY_KEY,
                request
        );

        // Assert response
        assertNotNull(response);

        // Sender: 500000 - 100000 = 400000
        assertEquals(
                0,
                new BigDecimal("400000").compareTo(
                        senderBalance.getAvailableBalance()
                )
        );

        // Receiver: 200000 + 100000 = 300000
        assertEquals(
                0,
                new BigDecimal("300000").compareTo(
                        receiverBalance.getAvailableBalance()
                )
        );

        // Sender ledger balance
        assertEquals(
                0,
                new BigDecimal("400000").compareTo(
                        senderBalance.getLedgerBalance()
                )
        );

        // Receiver ledger balance
        assertEquals(
                0,
                new BigDecimal("300000").compareTo(
                        receiverBalance.getLedgerBalance()
                )
        );

        // Verify transfer persistence
        verify(transferRepository).save(any(Transfer.class));

        // Verify debit and credit entries
        verify(transferEntryRepository).saveAll(anyList());

        // Verify idempotency
        verify(idempotencyService).save(
                eq(userId),
                eq(IDEMPOTENCY_KEY),
                any(UUID.class)
        );
    }

    // ==================================================
    // 2. DUPLICATE IDEMPOTENCY REQUEST
    // ==================================================

    @Test
    void shouldReturnExistingTransferForDuplicateRequest() {

        UUID transferId = UUID.randomUUID();

        Transfer existingTransfer = new Transfer();
        existingTransfer.setId(transferId);
        existingTransfer.setReferenceNumber("TRF-EXISTING");
        existingTransfer.setFromAccount(sender);
        existingTransfer.setToAccount(receiver);
        existingTransfer.setAmount(new BigDecimal("100000"));
        existingTransfer.setCurrency("IDR");
        existingTransfer.setStatus(TransferStatus.COMPLETED);

        IdempotencyRecord record = new IdempotencyRecord();
        record.setTransferId(transferId);

        when(idempotencyService.findExisting(userId, IDEMPOTENCY_KEY))
                .thenReturn(Optional.of(record));

        when(transferRepository.findById(transferId))
                .thenReturn(Optional.of(existingTransfer));

        TransferResponse response = transferService.transfer(
                userId,
                IDEMPOTENCY_KEY,
                validRequest()
        );

        assertNotNull(response);
        assertEquals(transferId, response.transferId());
        assertEquals("TRF-EXISTING", response.referenceNumber());

        // No new transfer
        verify(transferRepository, never()).save(any(Transfer.class));

        // No new entries
        verify(transferEntryRepository, never()).saveAll(any());

        // No new idempotency record
        verify(idempotencyService, never()).save(
                any(UUID.class),
                anyString(),
                any(UUID.class)
        );

        // No balance changes
        assertEquals(
                new BigDecimal("500000"),
                senderBalance.getAvailableBalance()
        );
    }

    // ==================================================
    // 3. INSUFFICIENT BALANCE
    // ==================================================

    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficient() {

        mockNoExistingRequest();
        mockCustomerAndSender();
        mockReceiver();
        mockBalances();

        TransferRequest request = new TransferRequest(
                receiver.getAccountNumber(),
                new BigDecimal("600000"),
                "Insufficient balance"
        );

        assertThrows(
                BusinessException.class,
                () -> transferService.transfer(
                        userId,
                        IDEMPOTENCY_KEY,
                        request
                )
        );

        // No transfer created
        verify(transferRepository, never()).save(any(Transfer.class));

        // No entries created
        verify(transferEntryRepository, never()).saveAll(any());

        // Balance remains unchanged
        assertEquals(
                new BigDecimal("500000"),
                senderBalance.getAvailableBalance()
        );

        assertEquals(
                new BigDecimal("100000"),
                receiverBalance.getAvailableBalance()
        );
    }

    // ==================================================
    // 4. SAME ACCOUNT TRANSFER
    // ==================================================

    @Test
    void shouldThrowExceptionWhenTransferringToSameAccount() {

        mockNoExistingRequest();
        mockCustomerAndSender();

        when(accountRepository.findByAccountNumber(sender.getAccountNumber()))
                .thenReturn(Optional.of(sender));

        TransferRequest request = new TransferRequest(
                sender.getAccountNumber(),
                new BigDecimal("100000"),
                "Same account"
        );

        assertThrows(
                BusinessException.class,
                () -> transferService.transfer(
                        userId,
                        IDEMPOTENCY_KEY,
                        request
                )
        );

        verify(accountBalanceRepository, never())
                .findByAccountIdForUpdate(any(UUID.class));

        verify(transferRepository, never()).save(any(Transfer.class));
    }

    // ==================================================
    // 5. RECEIVER ACCOUNT NOT FOUND
    // ==================================================

    @Test
    void shouldThrowExceptionWhenReceiverAccountDoesNotExist() {

        mockNoExistingRequest();
        mockCustomerAndSender();

        when(accountRepository.findByAccountNumber("999999999999"))
                .thenReturn(Optional.empty());

        TransferRequest request = new TransferRequest(
                "999999999999",
                new BigDecimal("100000"),
                "Unknown receiver"
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> transferService.transfer(
                        userId,
                        IDEMPOTENCY_KEY,
                        request
                )
        );

        verify(transferRepository, never()).save(any(Transfer.class));
    }

    // ==================================================
    // 6. CURRENCY MISMATCH
    // ==================================================

    @Test
    void shouldThrowExceptionWhenCurrenciesDoNotMatch() {

        mockNoExistingRequest();
        mockCustomerAndSender();
        mockReceiver();

        receiver.setCurrency("USD");

        TransferRequest request = validRequest();

        assertThrows(
                BusinessException.class,
                () -> transferService.transfer(
                        userId,
                        IDEMPOTENCY_KEY,
                        request
                )
        );

        verify(accountBalanceRepository, never())
                .findByAccountIdForUpdate(any(UUID.class));

        verify(transferRepository, never()).save(any(Transfer.class));
    }

    // ==================================================
    // 7. CUSTOMER NOT FOUND
    // ==================================================

    @Test
    void shouldThrowExceptionWhenCustomerDoesNotExist() {

        mockNoExistingRequest();

        when(customerRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> transferService.transfer(
                        userId,
                        IDEMPOTENCY_KEY,
                        validRequest()
                )
        );

        verify(accountRepository, never())
                .findByCustomerId(any(UUID.class));

        verify(transferRepository, never()).save(any(Transfer.class));
    }

    // ==================================================
    // 8. ZERO AMOUNT
    // ==================================================

    @Test
    void shouldThrowExceptionWhenAmountIsZero() {

        mockNoExistingRequest();

        TransferRequest request = new TransferRequest(
                receiver.getAccountNumber(),
                BigDecimal.ZERO,
                "Zero amount"
        );

        assertThrows(
                BusinessException.class,
                () -> transferService.transfer(
                        userId,
                        IDEMPOTENCY_KEY,
                        request
                )
        );

        verify(customerRepository, never())
                .findByUserId(any(UUID.class));

        verify(transferRepository, never()).save(any(Transfer.class));
    }
}
