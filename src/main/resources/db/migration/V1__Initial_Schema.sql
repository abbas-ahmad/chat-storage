-- Migration script to create initial database schema

-- Create chat_sessions table
CREATE TABLE chat_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    is_favorite BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create chat_messages table
CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    chat_session_id BIGINT NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
    sender_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    context TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

