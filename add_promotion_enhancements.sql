-- Add new columns to business_promotions table
ALTER TABLE business_promotions 
ADD COLUMN is_featured BOOLEAN DEFAULT FALSE,
ADD COLUMN image_url VARCHAR(500),
ADD COLUMN terms_and_conditions TEXT;

-- Create promotion_services junction table for many-to-many relationship
CREATE TABLE IF NOT EXISTS promotion_services (
    promotion_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    PRIMARY KEY (promotion_id, service_id),
    FOREIGN KEY (promotion_id) REFERENCES business_promotions(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_business_promotions_is_featured ON business_promotions(is_featured);
CREATE INDEX IF NOT EXISTS idx_business_promotions_dates ON business_promotions(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_business_promotions_type ON business_promotions(type);
CREATE INDEX IF NOT EXISTS idx_promotion_services_promotion_id ON promotion_services(promotion_id);
CREATE INDEX IF NOT EXISTS idx_promotion_services_service_id ON promotion_services(service_id);

-- Update existing promotions to have default values
UPDATE business_promotions SET is_featured = FALSE WHERE is_featured IS NULL;