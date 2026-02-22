CREATE TYPE visibility_type AS ENUM ('PUBLIC', 'PRIVATE', 'FRIENDS');

CREATE TYPE user_status_type AS ENUM ('ACTIVE', 'DORMANT', 'DELETED');

CREATE TABLE users
(
    id                UUID PRIMARY KEY          DEFAULT uuidv7(),
    email             VARCHAR(255) UNIQUE,
    last_login_at     TIMESTAMP,
    profile_image_url TEXT,
    status            user_status_type NOT NULL DEFAULT 'ACTIVE',
    username          VARCHAR(100) UNIQUE,
    visibility        visibility_type  NOT NULL DEFAULT 'PUBLIC',
    created_at        TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP
);

CREATE TABLE auth_providers
(
    id            SERIAL PRIMARY KEY,
    description   VARCHAR(255),
    provider_name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE user_auth_accounts
(
    id               UUID PRIMARY KEY DEFAULT uuidv7(),
    access_token     TEXT,
    provider_id      INT          NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    refresh_token    TEXT,
    token_expiry     TIMESTAMP,
    user_id          UUID         NOT NULL,
    created_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_provider FOREIGN KEY (provider_id) REFERENCES auth_providers (id) ON DELETE CASCADE,
    UNIQUE (provider_id, provider_user_id)
);

CREATE TABLE roles
(
    id        SERIAL PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE user_roles
(
    role_id INT  NOT NULL,
    user_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);
