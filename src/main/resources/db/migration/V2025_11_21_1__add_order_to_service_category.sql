ALTER TABLE service_category ADD COLUMN IF NOT EXISTS "order" BIGINT;

UPDATE service_category SET "order" = id WHERE "order" IS NULL;
