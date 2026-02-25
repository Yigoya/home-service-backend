-- Optional cleanup for legacy seed data from old initializer
-- Keeps only the plan names used by the current frontend catalog.
DELETE FROM subscription_features_english
WHERE plan_id IN (
    SELECT id FROM subscription_plans
    WHERE name IN ('Basic', 'Standard', 'Pro', 'Premium', 'Basic Tender', 'Pro Tender', 'Elite Tender')
);

DELETE FROM subscription_features_amharic
WHERE plan_id IN (
    SELECT id FROM subscription_plans
    WHERE name IN ('Basic', 'Standard', 'Pro', 'Premium', 'Basic Tender', 'Pro Tender', 'Elite Tender')
);

DELETE FROM subscription_features_oromo
WHERE plan_id IN (
    SELECT id FROM subscription_plans
    WHERE name IN ('Basic', 'Standard', 'Pro', 'Premium', 'Basic Tender', 'Pro Tender', 'Elite Tender')
);

DELETE FROM subscription_plans
WHERE name IN ('Basic', 'Standard', 'Pro', 'Premium', 'Basic Tender', 'Pro Tender', 'Elite Tender');
