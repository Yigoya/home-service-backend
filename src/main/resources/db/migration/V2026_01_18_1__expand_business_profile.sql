-- Expand business profile to capture richer compliance and categorization data
ALTER TABLE businesses
    ADD COLUMN IF NOT EXISTS name_amharic VARCHAR(255),
    ADD COLUMN IF NOT EXISTS alternative_contact_phone VARCHAR(255),
    ADD COLUMN IF NOT EXISTS registration_number VARCHAR(255),
    ADD COLUMN IF NOT EXISTS legal_representative_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS primary_category VARCHAR(255),
    ADD COLUMN IF NOT EXISTS local_distribution_network BOOLEAN NOT NULL DEFAULT FALSE;

-- Secondary categories as element collection
CREATE TABLE IF NOT EXISTS business_secondary_categories (
    business_id BIGINT NOT NULL,
    category VARCHAR(255),
    CONSTRAINT fk_business_secondary_categories_business FOREIGN KEY (business_id)
        REFERENCES businesses(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_business_secondary_categories_business_id
    ON business_secondary_categories(business_id);

-- Add kebele (sub-street) detail to business locations
ALTER TABLE business_locations
    ADD COLUMN IF NOT EXISTS kebele VARCHAR(255);
