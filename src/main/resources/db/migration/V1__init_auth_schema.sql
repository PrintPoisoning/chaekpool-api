CREATE TYPE user_visibility_type AS ENUM ('PUBLIC', 'PRIVATE', 'FRIENDS');

CREATE TYPE user_status_type AS ENUM ('ACTIVE', 'DORMANT', 'LEAVED', 'BANNED');

CREATE TABLE users
(
    id                UUID PRIMARY KEY              DEFAULT uuidv7(),
    email             VARCHAR(255) UNIQUE,
    username          VARCHAR(100) UNIQUE,
    profile_image_url TEXT,
    visibility        user_visibility_type NOT NULL DEFAULT 'PUBLIC',
    status            user_status_type     NOT NULL DEFAULT 'ACTIVE',
    last_login_at     TIMESTAMP,
    created_at        TIMESTAMP                     DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP
);

CREATE TABLE auth_providers
(
    id            UUID PRIMARY KEY DEFAULT uuidv7(),
    provider_name VARCHAR(50) UNIQUE NOT NULL,
    description   VARCHAR(255)
);

CREATE TABLE user_accounts
(
    user_id          UUID         NOT NULL,
    provider_id      UUID         NOT NULL,
    account_id       VARCHAR(255) NOT NULL,
    account_registry JSONB,
    auth_registry    JSONB,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_provider FOREIGN KEY (provider_id) REFERENCES auth_providers (id),
    PRIMARY KEY (user_id, provider_id),
    CONSTRAINT uk_user_accounts_provider_account UNIQUE (provider_id, account_id)
);

INSERT INTO auth_providers (provider_name, description)
VALUES ('CHAEKPOOL', '책풀 자체 로그인')
     , ('KAKAO', '카카오 소셜 로그인')
ON CONFLICT (provider_name) DO NOTHING;
