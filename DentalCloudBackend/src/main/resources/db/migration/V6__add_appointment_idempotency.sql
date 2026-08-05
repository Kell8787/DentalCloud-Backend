ALTER TABLE citas
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100);

CREATE UNIQUE INDEX IF NOT EXISTS uk_citas_idempotency_key
    ON citas (idempotency_key);
