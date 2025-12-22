ALTER TABLE business_locations
    ALTER COLUMN postal_code DROP NOT NULL,
    ALTER COLUMN state DROP NOT NULL;
