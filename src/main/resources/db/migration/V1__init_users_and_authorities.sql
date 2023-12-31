CREATE TABLE authorized_user (
    id INT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    name VARCHAR(25) NOT NULL UNIQUE,
    password VARCHAR(60) NOT NULL
);

CREATE TABLE user_authority (
    id INT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    type varchar(25) NOT NULL,
    scope TEXT
);


CREATE TABLE authorized_user__user_authority (
    user_id INT REFERENCES authorized_user(id),
    authority_id INT REFERENCES user_authority(id)
);




INSERT INTO user_authority (type, scope)
    VALUES ('ADMIN', NULL),
           ('READ', '*'),
           ('MODIFY', '*');
