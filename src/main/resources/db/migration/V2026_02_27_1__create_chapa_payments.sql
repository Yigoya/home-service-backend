CREATE TABLE IF NOT EXISTS chapa_payments (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    plan_id BIGINT NOT NULL,
    plan_type VARCHAR(64) NOT NULL,
    subscriber_type VARCHAR(64) NOT NULL,
    subscriber_id BIGINT NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(16) NOT NULL,
    tx_ref VARCHAR(255) UNIQUE,
    checkout_url TEXT,
    status VARCHAR(64) NOT NULL,
    raw_request TEXT,
    raw_response TEXT,
    callback_payload TEXT,
    callback_received_at TIMESTAMP,
    error_message VARCHAR(1024)
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_chapa_payments_plan_id'
          AND table_name = 'chapa_payments'
    ) THEN
        ALTER TABLE chapa_payments
            ADD CONSTRAINT fk_chapa_payments_plan_id
            FOREIGN KEY (plan_id) REFERENCES subscription_plans(id);
    END IF;
END$$;

CREATE INDEX IF NOT EXISTS idx_chapa_payments_tx_ref ON chapa_payments(tx_ref);
CREATE INDEX IF NOT EXISTS idx_chapa_payments_subscriber ON chapa_payments(subscriber_type, subscriber_id);
