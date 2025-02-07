-- Insert sample users into the "user" table
INSERT INTO "user" (name, email, password) VALUES
('Alice', 'alice@example.com', 'password123'),
('Bob', 'bob@example.com', 'password123'),
('Charlie', 'charlie@example.com', 'password123');

-- Insert sample rooms into the "room" table
INSERT INTO room (name) VALUES
('Room A'),
('Room B'),
('Room C');

-- Insert sample users_in_room data (assign users to rooms)
INSERT INTO user_in_room (user_id, room_id, is_still_a_member) VALUES
(1, 1, TRUE),  -- Alice in Room A
(2, 1, TRUE),  -- Bob in Room A
(3, 2, TRUE),  -- Charlie in Room B
(1, 3, TRUE);  -- Alice in Room C

-- Insert sample transaction_log data
INSERT INTO transaction_log (room_id, amount, description, transaction_timestamp) VALUES
(1, 100.50, 'Payment for Room A', '2025-02-01 10:00:00'),
(2, 200.75, 'Payment for Room B', '2025-02-02 11:00:00'),
(3, 150.00, 'Payment for Room C', '2025-02-03 12:00:00');

-- Insert sample payment_log data
INSERT INTO payment_log (from_user_id, to_user_id, amount, transaction_id) VALUES
(1, 2, 50.00, '1e24fdd1-52e4-4f09-8127-c15cbe8d62e3'),  -- Alice pays Bob for Room A
(2, 1, 60.00, '3b5a6213-c961-4c47-b876-556d56c728bb'),  -- Bob pays Alice for Room B
(3, 1, 75.00, '5d31b89a-0f50-41d4-b0e0-44348ed6edb0');  -- Charlie pays Alice for Room C
