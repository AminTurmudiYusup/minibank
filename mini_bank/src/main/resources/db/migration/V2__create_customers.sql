CREATE TABLE customers
(
    id         UUID PRIMARY KEY,

    user_id    UUID         NOT NULL,

    full_name  VARCHAR(150) NOT NULL,
    phone      VARCHAR(20) UNIQUE,
    address    VARCHAR(255),

    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uk_customers_user_id
        UNIQUE (user_id),

    CONSTRAINT fk_customers_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
);
