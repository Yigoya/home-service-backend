ALTER TABLE business_promotions
    ADD COLUMN IF NOT EXISTS category_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_business_promotions_category'
    ) THEN
        ALTER TABLE business_promotions
            ADD CONSTRAINT fk_business_promotions_category
                FOREIGN KEY (category_id)
                REFERENCES service_category(id);
    END IF;
END $$;
