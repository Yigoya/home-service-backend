-- Enforce unique phone numbers per account
-- Remove duplicate phone numbers (keep the lowest id per phone_number)
DELETE FROM users
WHERE id IN (
    SELECT id FROM (
        SELECT id,
               ROW_NUMBER() OVER (PARTITION BY phone_number ORDER BY id) AS rn
        FROM users
        WHERE phone_number IS NOT NULL
    ) t
    WHERE t.rn > 1
);

ALTER TABLE users
    ADD CONSTRAINT uq_users_phone_number UNIQUE (phone_number);
