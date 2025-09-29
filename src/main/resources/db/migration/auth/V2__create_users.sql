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
