-- insert default role (ROLE_USER) to all users
INSERT INTO users_roles (user_id, role_id)
SELECT id, 1 FROM users
ON CONFLICT DO NOTHING;

-- give user max and oscar admin roles
INSERT INTO users_roles (user_id, role_id)
VALUES (4, 2), (9, 2)
ON CONFLICT DO NOTHING;
