-- Make sender_id nullable and add contact fields for anonymous inquiries
ALTER TABLE inquiries
    ALTER COLUMN sender_id DROP NOT NULL;

ALTER TABLE inquiries
    ADD COLUMN IF NOT EXISTS contact_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS contact_email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(50);
