ALTER TABLE business_promotions
    ADD COLUMN IF NOT EXISTS category_id BIGINT;

ALTER TABLE business_promotions
    ADD CONSTRAINT IF NOT EXISTS fk_business_promotions_category
        FOREIGN KEY (category_id)
        REFERENCES service_category(id);
