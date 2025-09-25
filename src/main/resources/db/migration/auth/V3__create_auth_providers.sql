CREATE TABLE auth_providers
(
    id            SERIAL PRIMARY KEY,
    provider_name VARCHAR(50) UNIQUE NOT NULL,
    description   VARCHAR(255)
);
