-- Add product_condition column to products
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS product_condition VARCHAR(20);

-- Backfill existing products to NEW if null
UPDATE products
SET product_condition = 'NEW'
WHERE product_condition IS NULL;
