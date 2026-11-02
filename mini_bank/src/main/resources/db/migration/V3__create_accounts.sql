CREATE TABLE accounts
(
    id             UUID PRIMARY KEY,

    customer_id    UUID        NOT NULL,

    account_number VARCHAR(30) NOT NULL,
    account_type   VARCHAR(30) NOT NULL,
    currency       VARCHAR(3)  NOT NULL DEFAULT 'IDR',
    status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at     TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_accounts_account_number
        UNIQUE (account_number),

    CONSTRAINT fk_accounts_customer
        FOREIGN KEY (customer_id)
            REFERENCES customers (id)
);

CREATE INDEX idx_accounts_customer_id
    ON accounts (customer_id);
