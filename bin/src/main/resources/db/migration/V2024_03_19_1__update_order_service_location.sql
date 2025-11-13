-- Drop the existing foreign key constraint
ALTER TABLE orders DROP CONSTRAINT IF EXISTS fkbqightj1rvdd2nxjywn1abkl8;

-- Add the new foreign key constraint pointing to business_locations
ALTER TABLE orders ADD CONSTRAINT fk_order_business_location 
    FOREIGN KEY (service_location_id) REFERENCES business_locations(id); 