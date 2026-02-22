CREATE TYPE visibility_type AS ENUM ('PUBLIC', 'PRIVATE', 'FRIENDS');

CREATE TYPE user_status_type AS ENUM ('ACTIVE', 'DORMANT', 'DELETED');

CREATE TABLE users
(
    id                BIGINT PRIMARY KEY,
    username          VARCHAR(100) UNIQUE,
    email             VARCHAR(255) UNIQUE,
    profile_image_url TEXT,
    visibility        visibility_type  NOT NULL DEFAULT 'PUBLIC',
    created_at        TIMESTAMP                 DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP,
    last_login_at     TIMESTAMP,
    deleted_at        TIMESTAMP,
    status            user_status_type NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE auth_providers
(
    id            SERIAL PRIMARY KEY,
    provider_name VARCHAR(50) UNIQUE NOT NULL,
    description   VARCHAR(255)
);

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

CREATE TABLE roles
(
    id        SERIAL PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL,
    role_id INT    NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);
