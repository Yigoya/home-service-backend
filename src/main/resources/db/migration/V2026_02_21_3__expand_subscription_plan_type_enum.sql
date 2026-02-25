-- Expand plan_type check constraint for new subscription plan types
ALTER TABLE subscription_plans
    DROP CONSTRAINT IF EXISTS subscription_plans_plan_type_check;

ALTER TABLE subscription_plans
    ADD CONSTRAINT subscription_plans_plan_type_check
    CHECK (plan_type IN (
        'MARKETPLACE',
        'HOME_PROFESSIONAL',
        'TENDER',
        'YELLOW_PAGES',
        'JOBS',
        'TECHNICIAN',
        'BUSINESS',
        'CUSTOMER_TENDER'
    ));