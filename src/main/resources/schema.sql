CREATE TABLE "user" (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,      -- nullable for unregistered users
    password VARCHAR(255)           -- nullable
);

CREATE TABLE room (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE users_in_room (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    room_id INT NOT NULL,
    is_still_a_member BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE,
    UNIQUE (user_id, room_id)       -- Ensures a user can only be added once to a room
);

CREATE TABLE transaction_log (
    transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    description TEXT,
    transaction_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE
);

CREATE INDEX idx_transaction_timestamp ON transaction_log(transaction_timestamp DESC);

CREATE TABLE payment_log (
    id SERIAL PRIMARY KEY,
    from_user_id INT NOT NULL,
    to_user_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    transaction_id UUID NOT NULL,
    FOREIGN KEY (from_user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (to_user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (transaction_id) REFERENCES transaction_log(transaction_id) ON DELETE CASCADE
);
