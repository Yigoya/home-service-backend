-- Update ApplicationStatus enum values in the database
-- Run these queries to migrate from old status values to new ones

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

-- REJECTED stays the same (no update needed)
-- UPDATE job_applications SET status = 'REJECTED' WHERE status = 'REJECTED';

-- Update ACCEPTED to HIRED
UPDATE job_applications 
SET status = 'HIRED' 
WHERE status = 'ACCEPTED';

-- Verify the updates
SELECT status, COUNT(*) as count 
FROM job_applications 
GROUP BY status 
ORDER BY status;