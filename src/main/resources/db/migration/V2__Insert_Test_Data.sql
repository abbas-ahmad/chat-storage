-- Explicitly set IDs for chat_sessions to ensure foreign key references are valid
INSERT INTO chat_sessions (id, user_id, name, is_favorite, created_at, updated_at) VALUES
(1, 'user1', 'Session 1', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'user2', 'Session 2', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'user3', 'Session 3', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'user4', 'Session 4', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test data into chat_messages table
INSERT INTO chat_messages (chat_session_id, sender_type, content, context, created_at) VALUES
(1, 'USER', 'Hello, this is a test message.', NULL, CURRENT_TIMESTAMP),
(1, 'ASSISTANT', 'This is a system message.', NULL, CURRENT_TIMESTAMP),
(2, 'USER', 'Another test message.', NULL, CURRENT_TIMESTAMP),
(3, 'USER', 'Test message for session 3.', NULL, CURRENT_TIMESTAMP),
(3, 'ASSISTANT', 'System message for session 3.', NULL, CURRENT_TIMESTAMP),
(4, 'USER', 'Test message for session 4.', NULL, CURRENT_TIMESTAMP),
(4, 'ASSISTANT', 'System message for session 4.', NULL, CURRENT_TIMESTAMP);
