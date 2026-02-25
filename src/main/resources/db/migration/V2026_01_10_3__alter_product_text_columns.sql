-- Alter product text fields to TEXT for longer content
ALTER TABLE products
    ALTER COLUMN description TYPE TEXT,
    ALTER COLUMN specifications TYPE TEXT;
