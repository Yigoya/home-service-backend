CREATE TABLE IF NOT EXISTS telebirr_payments (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    plan_id BIGINT NOT NULL,
    plan_type VARCHAR(50) NOT NULL,
    subscriber_type VARCHAR(50) NOT NULL,
    subscriber_id BIGINT NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'ETB',
    merchant_order_id VARCHAR(100) UNIQUE,
    prepay_id VARCHAR(100),
    checkout_url TEXT,
    status VARCHAR(30) NOT NULL,
    raw_request TEXT,
    raw_response TEXT,
    callback_payload TEXT,
    callback_received_at TIMESTAMP,
    error_message TEXT,
    CONSTRAINT fk_telebirr_payments_plan
        FOREIGN KEY (plan_id) REFERENCES subscription_plans(id)
            ON DELETE CASCADE
);