CREATE TABLE transfers
(
    id               UUID PRIMARY KEY,

    reference_number VARCHAR(50)    NOT NULL,

    from_account_id  UUID           NOT NULL,
    to_account_id    UUID           NOT NULL,

    amount           NUMERIC(19, 2) NOT NULL,
    currency         VARCHAR(3)     NOT NULL DEFAULT 'IDR',

    status           VARCHAR(20)    NOT NULL,

    description      VARCHAR(255),

    created_at       TIMESTAMPTZ    NOT NULL,

    CONSTRAINT uk_transfers_reference_number
        UNIQUE (reference_number),

    CONSTRAINT fk_transfers_from_account
        FOREIGN KEY (from_account_id)
            REFERENCES accounts (id),

    CONSTRAINT fk_transfers_to_account
        FOREIGN KEY (to_account_id)
            REFERENCES accounts (id),

    CONSTRAINT chk_transfers_amount_positive
        CHECK (amount > 0),

    CONSTRAINT chk_transfers_different_accounts
        CHECK (from_account_id <> to_account_id)
);

CREATE INDEX idx_transfers_from_account
    ON transfers (from_account_id, created_at);

CREATE INDEX idx_transfers_to_account
    ON transfers (to_account_id, created_at);
