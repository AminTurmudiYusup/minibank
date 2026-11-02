package com.example.minibank.transaction.entity;


import com.example.minibank.account.entity.Account;
import com.example.minibank.common.enums.TransferEntryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "transfer_entries",
        indexes = {
                @Index(
                        name = "idx_transfer_entries_transfer",
                        columnList = "transfer_id"
                ),
                @Index(
                        name = "idx_transfer_entries_account",
                        columnList = "account_id, created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class TransferEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transfer_id", nullable = false)
    private Transfer transfer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    private TransferEntryType entryType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}

