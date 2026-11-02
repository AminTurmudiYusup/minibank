package com.example.minibank.transaction.repository;

import com.example.minibank.transaction.entity.TransferEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransferEntryRepository
        extends JpaRepository<TransferEntry, UUID> {

    List<TransferEntry> findByTransferId(UUID transferId);
}

