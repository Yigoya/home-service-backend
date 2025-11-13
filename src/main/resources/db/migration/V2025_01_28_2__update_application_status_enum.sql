-- Update ApplicationStatus enum values
-- Migrate from old status values to new ones

-- Update SUBMITTED to PENDING
UPDATE job_applications 
SET status = 'PENDING' 
WHERE status = 'SUBMITTED';

-- Update VIEWED to REVIEWED
UPDATE job_applications 
SET status = 'REVIEWED' 
WHERE status = 'VIEWED';

-- Update UNDER_REVIEW to REVIEWED
UPDATE job_applications 
SET status = 'REVIEWED' 
WHERE status = 'UNDER_REVIEW';

-- Update ACCEPTED to HIRED
UPDATE job_applications 
SET status = 'HIRED' 
WHERE status = 'ACCEPTED';

-- Add check constraint for new enum values (optional)
-- ALTER TABLE job_applications DROP CONSTRAINT IF EXISTS job_applications_status_check;
-- ALTER TABLE job_applications ADD CONSTRAINT job_applications_status_check 
--     CHECK (status IN ('PENDING', 'REVIEWED', 'SHORTLISTED', 'REJECTED', 'HIRED'));