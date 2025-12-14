ALTER TABLE tender
    ADD COLUMN IF NOT EXISTS reference_number VARCHAR(255),
    ADD COLUMN IF NOT EXISTS notice_number VARCHAR(255),
    ADD COLUMN IF NOT EXISTS product_category VARCHAR(255),
    ADD COLUMN IF NOT EXISTS tender_type VARCHAR(255),
    ADD COLUMN IF NOT EXISTS procurement_method VARCHAR(255),
    ADD COLUMN IF NOT EXISTS cost_of_tender_document VARCHAR(255),
    ADD COLUMN IF NOT EXISTS bid_validity VARCHAR(255),
    ADD COLUMN IF NOT EXISTS bid_security VARCHAR(255),
    ADD COLUMN IF NOT EXISTS contract_period VARCHAR(255),
    ADD COLUMN IF NOT EXISTS performance_security VARCHAR(255),
    ADD COLUMN IF NOT EXISTS payment_terms TEXT,
    ADD COLUMN IF NOT EXISTS key_deliverables TEXT,
    ADD COLUMN IF NOT EXISTS technical_specifications TEXT;
