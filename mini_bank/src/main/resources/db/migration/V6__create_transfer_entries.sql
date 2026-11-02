CREATE TABLE transfer_entries
(
    id          UUID PRIMARY KEY,

    transfer_id UUID           NOT NULL,
    account_id  UUID           NOT NULL,

    entry_type  VARCHAR(10)    NOT NULL,
    amount      NUMERIC(19, 2) NOT NULL,

    created_at  TIMESTAMPTZ    NOT NULL,

    CONSTRAINT fk_transfer_entries_transfer
        FOREIGN KEY (transfer_id)
            REFERENCES transfers (id),

    CONSTRAINT fk_transfer_entries_account
        FOREIGN KEY (account_id)
            REFERENCES accounts (id),

    CONSTRAINT chk_transfer_entries_amount_positive
        CHECK (amount > 0)
);

CREATE INDEX idx_transfer_entries_transfer
    ON transfer_entries (transfer_id);

CREATE INDEX idx_transfer_entries_account
    ON transfer_entries (account_id, created_at);
