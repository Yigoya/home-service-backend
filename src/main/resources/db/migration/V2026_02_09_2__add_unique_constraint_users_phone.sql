-- Enforce unique phone numbers per account
-- Normalize duplicate phone numbers (keep the lowest id per phone_number).
-- Do not delete rows to avoid breaking foreign-key references from profile tables.
-- Keep NOT NULL valid by rewriting duplicate values with a deterministic suffix.
WITH duplicated_users AS (
    SELECT id,
           phone_number,
           ROW_NUMBER() OVER (PARTITION BY phone_number ORDER BY id) AS rn
    FROM users
    WHERE phone_number IS NOT NULL
)
UPDATE users u
SET phone_number = du.phone_number || '_dup_' || du.id
FROM duplicated_users du
WHERE u.id = du.id
  AND du.rn > 1;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_users_phone_number'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT uq_users_phone_number UNIQUE (phone_number);
    END IF;
END $$;
