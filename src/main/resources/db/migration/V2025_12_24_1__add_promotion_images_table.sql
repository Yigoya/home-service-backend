-- Create table to store multiple images per promotion
CREATE TABLE IF NOT EXISTS promotion_images (
    promotion_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    FOREIGN KEY (promotion_id) REFERENCES business_promotions(id) ON DELETE CASCADE
);

-- Optional backfill from legacy single-column image_url if present
INSERT INTO promotion_images (promotion_id, image_url)
SELECT id, image_url FROM business_promotions WHERE image_url IS NOT NULL;

-- Index for faster lookups
CREATE INDEX IF NOT EXISTS idx_promotion_images_promotion_id ON promotion_images(promotion_id);
