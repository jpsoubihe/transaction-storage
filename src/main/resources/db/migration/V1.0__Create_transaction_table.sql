CREATE TABLE IF NOT EXISTS transaction (
    transaction_id VARCHAR(255) NOT NULL,
    description VARCHAR(50) NOT NULL,
    amount decimal NOT NULL DEFAULT 0,
    transaction_date VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (transaction_id)
);
