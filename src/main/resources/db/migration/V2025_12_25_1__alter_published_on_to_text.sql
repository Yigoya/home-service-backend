ALTER TABLE tender
    ALTER COLUMN published_on TYPE TEXT USING published_on::text;
