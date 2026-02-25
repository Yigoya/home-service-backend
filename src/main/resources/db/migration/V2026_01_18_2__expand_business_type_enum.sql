-- Allow new business types (WHOLESALER, OTHER) in check constraint
ALTER TABLE businesses DROP CONSTRAINT IF EXISTS businesses_business_type_check;
ALTER TABLE businesses
    ADD CONSTRAINT businesses_business_type_check
    CHECK (business_type IN (
        'B2B',
        'SERVICE',
        'RETAIL',
        'MANUFACTURER',
        'SUPPLIER',
        'DISTRIBUTOR',
        'SERVICE_PROVIDER',
        'WHOLESALER',
        'OTHER'
    ));
