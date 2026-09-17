-- Preserve pre-existing development data under a non-login legacy account.
INSERT INTO app_users (email, password_hash, created_at, updated_at)
SELECT 'legacy-reading-sessions@system.invalid',
       '!',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP WHERE NOT EXISTS (
    SELECT 1 FROM app_users WHERE email = 'legacy-reading-sessions@system.invalid'
);

ALTER TABLE reading_sessions
    ADD COLUMN owner_id BIGINT;
ALTER TABLE reading_sessions
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE reading_sessions
    ADD COLUMN created_by_user_id BIGINT;
ALTER TABLE reading_sessions
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE reading_sessions
    ADD COLUMN updated_by_user_id BIGINT;

UPDATE reading_sessions
SET owner_id           = (SELECT id FROM app_users WHERE email = 'legacy-reading-sessions@system.invalid'),
    created_at         = CURRENT_TIMESTAMP,
    created_by_user_id = (SELECT id FROM app_users WHERE email = 'legacy-reading-sessions@system.invalid'),
    updated_at         = CURRENT_TIMESTAMP,
    updated_by_user_id = (SELECT id FROM app_users WHERE email = 'legacy-reading-sessions@system.invalid');

ALTER TABLE reading_sessions
    ALTER COLUMN owner_id SET NOT NULL;
ALTER TABLE reading_sessions
    ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE reading_sessions
    ALTER COLUMN created_by_user_id SET NOT NULL;
ALTER TABLE reading_sessions
    ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE reading_sessions
    ALTER COLUMN updated_by_user_id SET NOT NULL;
ALTER TABLE reading_sessions
    ADD CONSTRAINT fk_reading_sessions_owner
        FOREIGN KEY (owner_id) REFERENCES app_users (id);
ALTER TABLE reading_sessions
    ADD CONSTRAINT fk_reading_sessions_created_by_user
        FOREIGN KEY (created_by_user_id) REFERENCES app_users (id);
ALTER TABLE reading_sessions
    ADD CONSTRAINT fk_reading_sessions_updated_by_user
        FOREIGN KEY (updated_by_user_id) REFERENCES app_users (id);

CREATE INDEX idx_reading_sessions_owner_id
    ON reading_sessions (owner_id, id);

CREATE INDEX idx_reading_sessions_owner_book_status
    ON reading_sessions (owner_id, book_id, status);
