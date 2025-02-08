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

CREATE TABLE user_in_room (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    room_id INT NOT NULL,
    is_still_a_member BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE,
    UNIQUE (user_id, room_id)       -- Ensures a user can only be added once to a room
);

CREATE TABLE payment (
    payment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    description TEXT,
    is_repayment BOOLEAN NOT NULL DEFAULT FALSE,
    payment_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE
);

CREATE INDEX idx_payment_timestamp ON payment(payment_timestamp DESC);

CREATE TABLE payment_record (
    id SERIAL PRIMARY KEY,
    from_user_id INT NOT NULL,
    to_user_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    payment_id UUID NOT NULL,
    is_credit BOOLEAN NOT NULL,  -- Replaces 'direction' for better clarity
    FOREIGN KEY (from_user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (to_user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (payment_id) REFERENCES payment(payment_id) ON DELETE CASCADE,
    UNIQUE (payment_id, from_user_id, to_user_id)
);

-- FIXME
--from user -> to user !is_credit
--to user -> from user is_credit
--be careful when querying

--SELECT user_1, user_2,
--       SUM(CASE WHEN direction = 'user_1 → user_2' THEN amount ELSE -amount END) AS net_balance
--FROM transactions
--WHERE user_1 = ? AND user_2 = ?
--GROUP BY user_1, user_2;


CREATE INDEX idx_from_user ON payment_record (from_user_id);
CREATE INDEX idx_to_user ON payment_record (to_user_id);
CREATE INDEX idx_room ON payment (room_id);
CREATE INDEX idx_payment_users ON payment_record (from_user_id, to_user_id);

CREATE VIEW room_payment_log AS
SELECT
    p.payment_id,
    p.room_id,
    p.amount AS total_amount,
    p.description,
    p.is_repayment,
    p.payment_timestamp,
    pr.from_user_id,
    pr.to_user_id,
    pr.amount AS user_amount,
    pr.is_credit
FROM payment p
LEFT JOIN payment_record pr ON p.payment_id = pr.payment_id;
