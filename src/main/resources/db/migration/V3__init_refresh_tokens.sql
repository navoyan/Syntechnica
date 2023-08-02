CREATE TABLE refresh_token (
    value UUID PRIMARY KEY,
    user_id INT REFERENCES authorized_user(id) ON DELETE CASCADE,
    family BIGINT NOT NULL,
    creation_timestamp TIMESTAMP WITH TIME ZONE,
    expiration_timestamp TIMESTAMP WITH TIME ZONE
);

CREATE INDEX ON refresh_token (user_id, family, creation_timestamp);

