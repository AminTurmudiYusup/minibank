package com.example.minibank.transaction.service;

import com.example.minibank.transaction.entity.IdempotencyRecord;
import com.example.minibank.transaction.repository.IdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;

    public Optional<IdempotencyRecord> findExisting(
            UUID userId,
            String key) {

        return repository.findByUserIdAndIdempotencyKey(userId, key);
    }

    public void save(
            UUID userId,
            String key,
            UUID transferId) {

        IdempotencyRecord record = new IdempotencyRecord();
        record.setUserId(userId);
        record.setIdempotencyKey(key);
        record.setTransferId(transferId);

        repository.save(record);
    }
}