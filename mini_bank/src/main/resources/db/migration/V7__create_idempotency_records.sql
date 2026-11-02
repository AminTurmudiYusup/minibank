CREATE TABLE idempotency_records
(
    id              UUID PRIMARY KEY,

    user_id         UUID         NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    transfer_id     UUID         NOT NULL,

    created_at      TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uk_idempotency_user_key
        UNIQUE (user_id, idempotency_key),

    CONSTRAINT fk_idempotency_records_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT fk_idempotency_records_transfer
        FOREIGN KEY (transfer_id)
            REFERENCES transfers (id),

    CONSTRAINT uk_idempotency_records_transfer
        UNIQUE (transfer_id)
);
