-- Backfill existing tenders to ensure is_free has no NULLs and enforce NOT NULL
UPDATE tender SET is_free = FALSE WHERE is_free IS NULL;

-- Ensure default persists for future inserts
ALTER TABLE tender ALTER COLUMN is_free SET DEFAULT FALSE;

-- Enforce not null to avoid primitive boolean mapping issues
ALTER TABLE tender ALTER COLUMN is_free SET NOT NULL;
