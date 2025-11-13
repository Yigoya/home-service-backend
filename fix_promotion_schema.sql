-- Add missing is_featured column to business_promotions table
ALTER TABLE business_promotions ADD COLUMN IF NOT EXISTS is_featured BOOLEAN DEFAULT FALSE;

-- Add terms_and_conditions column if it doesn't exist
ALTER TABLE business_promotions ADD COLUMN IF NOT EXISTS terms_and_conditions TEXT;

-- Update existing promotions to have default values
UPDATE business_promotions SET is_featured = FALSE WHERE is_featured IS NULL;

-- Add indexes for better performance (only if they don't exist)
CREATE INDEX IF NOT EXISTS idx_business_promotions_is_featured ON business_promotions(is_featured);
CREATE INDEX IF NOT EXISTS idx_business_promotions_dates ON business_promotions(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_business_promotions_type ON business_promotions(type);