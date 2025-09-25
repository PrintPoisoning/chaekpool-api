CREATE TABLE user_auth_accounts
(
    id               BIGINT PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    provider_id      INT          NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    access_token     TEXT,
    refresh_token    TEXT,
    token_expiry     TIMESTAMP    NULL,
    linked_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_provider FOREIGN KEY (provider_id) REFERENCES auth_providers (id) ON DELETE CASCADE,
    UNIQUE (provider_id, provider_user_id)
);
