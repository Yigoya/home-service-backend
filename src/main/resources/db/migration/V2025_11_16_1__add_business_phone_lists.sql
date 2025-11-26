-- Create tables to store multiple telephone and mobile numbers per business
CREATE TABLE business_telephone_numbers (
    business_id BIGINT NOT NULL,
    telephone VARCHAR(255),
    CONSTRAINT fk_business_telephone_numbers_business FOREIGN KEY (business_id)
        REFERENCES businesses(id) ON DELETE CASCADE
);

CREATE INDEX idx_business_telephone_numbers_business_id ON business_telephone_numbers(business_id);

CREATE TABLE business_mobile_numbers (
    business_id BIGINT NOT NULL,
    mobile VARCHAR(255),
    CONSTRAINT fk_business_mobile_numbers_business FOREIGN KEY (business_id)
        REFERENCES businesses(id) ON DELETE CASCADE
);

CREATE INDEX idx_business_mobile_numbers_business_id ON business_mobile_numbers(business_id);
