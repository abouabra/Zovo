-- 1. Channels Table
CREATE TABLE channels
(
    id         UUID PRIMARY KEY,
    type       VARCHAR(10)                           NOT NULL CHECK (type IN ('personal', 'group')),
    name       VARCHAR(255)                          NOT NULL UNIQUE,
    avatar_key VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 2. Messages Table
CREATE TABLE messages
(
    id         UUID PRIMARY KEY,
    channel_id UUID                                  NOT NULL REFERENCES channels (id) ON DELETE CASCADE,
    sender_id  INTEGER                               NOT NULL REFERENCES users (id),
    content    TEXT                                  NOT NULL,
    timestamp  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 3. Channel Membership Table (for group channels)
CREATE TABLE channel_members
(
    channel_id UUID    NOT NULL REFERENCES channels (id) ON DELETE CASCADE,
    user_id    INTEGER NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (channel_id, user_id)
);
