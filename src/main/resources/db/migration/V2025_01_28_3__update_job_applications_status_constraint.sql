-- Update the check constraint for job_applications status to use new enum values

-- First, drop the existing check constraint
ALTER TABLE job_applications DROP CONSTRAINT IF EXISTS job_applications_status_check;

-- Update existing data to use new enum values (in case migration V2025_01_28_2 wasn't run)
UPDATE job_applications SET status = 'PENDING' WHERE status = 'SUBMITTED';
UPDATE job_applications SET status = 'REVIEWED' WHERE status = 'VIEWED';
UPDATE job_applications SET status = 'REVIEWED' WHERE status = 'UNDER_REVIEW';
UPDATE job_applications SET status = 'HIRED' WHERE status = 'ACCEPTED';
-- REJECTED stays the same

-- Add new check constraint with updated enum values
ALTER TABLE job_applications ADD CONSTRAINT job_applications_status_check 
    CHECK (status IN ('PENDING', 'REVIEWED', 'SHORTLISTED', 'REJECTED', 'HIRED'));