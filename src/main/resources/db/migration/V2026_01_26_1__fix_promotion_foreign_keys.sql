-- Correct foreign keys for promotions
-- Drops incorrect references to services table and re-links to business_promotions/service_category

-- Fix promotion_services.promotion_id -> business_promotions
ALTER TABLE IF EXISTS promotion_services
    DROP CONSTRAINT IF EXISTS fkbeyiqmjj9tved1y3axrjlntq0;
ALTER TABLE IF EXISTS promotion_services
    ADD CONSTRAINT fk_promotion_services_promotion
        FOREIGN KEY (promotion_id) REFERENCES business_promotions(id) ON DELETE CASCADE;

-- Fix promotion_images.promotion_id -> business_promotions
ALTER TABLE IF EXISTS promotion_images
    DROP CONSTRAINT IF EXISTS fkb0k0wky0q9ls1qb3u42svhrqm;
ALTER TABLE IF EXISTS promotion_images
    ADD CONSTRAINT fk_promotion_images_promotion
        FOREIGN KEY (promotion_id) REFERENCES business_promotions(id) ON DELETE CASCADE;

-- Fix business_promotions.category_id -> service_category
ALTER TABLE IF EXISTS business_promotions
    DROP CONSTRAINT IF EXISTS fkkt94yinxkn2ny0c8iu5vity4g;
ALTER TABLE IF EXISTS business_promotions
    DROP CONSTRAINT IF EXISTS fk_business_promotions_category;
ALTER TABLE IF EXISTS business_promotions
    ADD CONSTRAINT fk_business_promotions_category
        FOREIGN KEY (category_id) REFERENCES service_category(id) ON DELETE SET NULL;
