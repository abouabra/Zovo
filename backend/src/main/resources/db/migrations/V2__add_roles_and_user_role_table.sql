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
