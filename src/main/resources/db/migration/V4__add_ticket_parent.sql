ALTER TABLE tickets
    ADD COLUMN parent_id UUID;

ALTER TABLE tickets
    ADD CONSTRAINT fk_ticket_parent
        FOREIGN KEY (parent_id)
            REFERENCES tickets (id)
            ON DELETE SET NULL;