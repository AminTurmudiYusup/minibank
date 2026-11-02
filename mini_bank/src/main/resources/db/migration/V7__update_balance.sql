UPDATE account_balances
SET available_balance = 5000000.00,
    ledger_balance    = 5000000.00
WHERE account_id = (SELECT id
                    FROM accounts
                    WHERE account_number = '879885249608');

UPDATE account_balances
SET available_balance = 5000000.00,
    ledger_balance    = 5000000.00
WHERE account_id = (SELECT id
                    FROM accounts
                    WHERE account_number = '712438321990');