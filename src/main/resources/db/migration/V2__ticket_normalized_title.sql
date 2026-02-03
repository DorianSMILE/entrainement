ALTER TABLE tickets
    ADD COLUMN IF NOT EXISTS normalized_title text;

UPDATE tickets
SET normalized_title = lower(regexp_replace(trim(title), '\s+', ' ', 'g'))
WHERE normalized_title IS NULL;

CREATE INDEX IF NOT EXISTS idx_tickets_normalized_title
    ON tickets (normalized_title);
