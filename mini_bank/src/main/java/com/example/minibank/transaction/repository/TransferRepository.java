package com.example.minibank.transaction.repository;


import com.example.minibank.transaction.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransferRepository
        extends JpaRepository<Transfer, UUID> {

    Optional<Transfer> findByReferenceNumber(String referenceNumber);
}

