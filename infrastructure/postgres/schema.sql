-- Create the users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    "createdAt" TIMESTAMPTZ DEFAULT now(),
    "isActive" BOOLEAN NOT NULL DEFAULT FALSE,
    "isEnabled" BOOLEAN NOT NULL DEFAULT FALSE,
    two_fa_enabled BOOLEAN DEFAULT FALSE,
    two_fa_secret VARCHAR(255),
    two_fa_recovery_codes TEXT
);

-- Create the roles table
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Create the join table for users and roles
CREATE TABLE users_roles (
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create the verification_tokens table
CREATE TABLE verification_tokens (
    id SERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id INTEGER NOT NULL,
    type VARCHAR(255) NOT NULL,
    "expiredAt" TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Seed default roles
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');
