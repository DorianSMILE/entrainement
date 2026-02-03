CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_tickets_title_trgm
    ON tickets USING gin (title gin_trgm_ops);