-- Add is_free column to tender table to indicate free tenders
ALTER TABLE tender ADD COLUMN IF NOT EXISTS is_free BOOLEAN DEFAULT FALSE;