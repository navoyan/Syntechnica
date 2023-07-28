CREATE TABLE access_token_metadata (
    user_id INT PRIMARY KEY REFERENCES authorized_user(id) ON DELETE CASCADE,
    version BIGINT NOT NULL
);

CREATE INDEX ON authorized_user (name);
