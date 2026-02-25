-- Create table to store multiple images per promotion
CREATE TABLE IF NOT EXISTS promotion_images (
    promotion_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    FOREIGN KEY (promotion_id) REFERENCES business_promotions(id) ON DELETE CASCADE
);

-- Optional backfill from legacy single-column image_url if present
INSERT INTO promotion_images (promotion_id, image_url)
SELECT bp.id, bp.image_url
FROM business_promotions bp
WHERE bp.image_url IS NOT NULL
    AND NOT EXISTS (
        SELECT 1
        FROM promotion_images pi
        WHERE pi.promotion_id = bp.id
            AND pi.image_url = bp.image_url
    )
    AND (
        -- If an existing drifted schema wired promotion_images to services(id), only backfill valid IDs.
        EXISTS (SELECT 1 FROM services s WHERE s.id = bp.id)
        OR NOT EXISTS (
            SELECT 1
            FROM pg_constraint c
            JOIN pg_class child_table ON child_table.oid = c.conrelid
            JOIN pg_class parent_table ON parent_table.oid = c.confrelid
            WHERE c.contype = 'f'
                AND child_table.relname = 'promotion_images'
                AND parent_table.relname = 'services'
        )
    );

-- Index for faster lookups
CREATE INDEX IF NOT EXISTS idx_promotion_images_promotion_id ON promotion_images(promotion_id);
