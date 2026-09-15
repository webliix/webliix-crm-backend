CREATE TABLE roles
(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE
);

CREATE TABLE users
(
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    phone VARCHAR(50),
    enabled BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE user_roles
(
    user_id BIGINT,
    role_id BIGINT,

    CONSTRAINT fk_user
        FOREIGN KEY(user_id)
        REFERENCES users(id),

    CONSTRAINT fk_role
        FOREIGN KEY(role_id)
        REFERENCES roles(id)
);
