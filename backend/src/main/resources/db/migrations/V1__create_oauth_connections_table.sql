CREATE TABLE oauth_connections (
   id BIGSERIAL PRIMARY KEY,
   user_id BIGINT NOT NULL,
   provider VARCHAR(20) NOT NULL,
   provider_id VARCHAR(255) NOT NULL,
   email VARCHAR(255),
   name VARCHAR(255),
   created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
   CONSTRAINT unique_user_provider UNIQUE (user_id, provider),
   CONSTRAINT unique_provider_id UNIQUE (provider, provider_id)
);