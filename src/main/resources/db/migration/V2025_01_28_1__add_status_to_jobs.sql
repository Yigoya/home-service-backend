-- Add status column to jobs table
ALTER TABLE jobs ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Add check constraint to ensure only valid status values
ALTER TABLE jobs ADD CONSTRAINT jobs_status_check 
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'CLOSED', 'DRAFT'));

-- Create index on status column for better query performance
CREATE INDEX idx_jobs_status ON jobs(status);

-- Update any existing jobs to have ACTIVE status (if needed)
UPDATE jobs SET status = 'ACTIVE' WHERE status IS NULL;