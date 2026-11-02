CREATE TABLE account_balances
(
    id                UUID PRIMARY KEY,

    account_id        UUID           NOT NULL,

    available_balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    ledger_balance    NUMERIC(19, 2) NOT NULL DEFAULT 0.00,

    version           BIGINT         NOT NULL DEFAULT 0,

    updated_at        TIMESTAMPTZ    NOT NULL,

    CONSTRAINT uk_account_balances_account
        UNIQUE (account_id),

    CONSTRAINT fk_account_balances_account
        FOREIGN KEY (account_id)
            REFERENCES accounts (id),

    CONSTRAINT chk_available_balance_non_negative
        CHECK (available_balance >= 0),

    CONSTRAINT chk_ledger_balance_non_negative
        CHECK (ledger_balance >= 0)
);
