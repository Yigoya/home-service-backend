-- Allow phone-only signup by making users.email nullable
ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
