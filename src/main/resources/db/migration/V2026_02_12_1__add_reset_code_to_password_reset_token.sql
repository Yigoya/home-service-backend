-- Add SMS reset code to password reset tokens
ALTER TABLE password_reset_token
    ADD COLUMN IF NOT EXISTS code VARCHAR(6);
