-- Update the users_role_check constraint to include new roles
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

-- Add the updated constraint with all roles including JOB_COMPANY and JOB_SEEKER
ALTER TABLE users ADD CONSTRAINT users_role_check 
CHECK (role IN ('CUSTOMER', 'TECHNICIAN', 'OPERATOR', 'ADMIN', 'USER', 'AGENCY', 'JOB_COMPANY', 'JOB_SEEKER'));
