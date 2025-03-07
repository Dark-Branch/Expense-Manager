CREATE TABLE "user" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE room (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL
);

CREATE TABLE user_in_room (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,                         -- Nullable for unregistered users
    room_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,          -- Display name for the user in this room
    is_still_a_member BOOLEAN DEFAULT TRUE,
    is_admin BOOLEAN DEFAULT FALSE,
    is_registered BOOLEAN DEFAULT FALSE,
    -- FIXME: is this needed
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE,
    UNIQUE (user_id, room_id)       -- Ensures a user can only be added once to a room
);

CREATE TABLE payment (
    payment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    description TEXT,
    is_repayment BOOLEAN DEFAULT FALSE,
    payment_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE CASCADE
);

CREATE INDEX idx_payment_timestamp ON payment(payment_timestamp DESC);

CREATE TABLE payment_record (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- from_id < to_id
    from_user_in_room_id UUID NOT NULL,
    to_user_in_room_id UUID NOT NULL,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    payment_id UUID NOT NULL,
    FOREIGN KEY (from_user_in_room_id) REFERENCES user_in_room(id) ON DELETE CASCADE,
    FOREIGN KEY (to_user_in_room_id) REFERENCES user_in_room(id) ON DELETE CASCADE,
    FOREIGN KEY (payment_id) REFERENCES payment(payment_id) ON DELETE CASCADE,
    UNIQUE (payment_id, from_user_in_room_id, to_user_in_room_id)
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

-- FIXME: first can be removed because combinded index covers that also
CREATE INDEX idx_from_user ON payment_record (from_user_in_room_id);
CREATE INDEX idx_to_user ON payment_record (to_user_in_room_id);
CREATE INDEX idx_room ON payment (room_id);
CREATE INDEX idx_payment_users ON payment_record (from_user_in_room_id, to_user_in_room_id);

CREATE VIEW room_payment_log AS
SELECT
    p.payment_id,
    p.room_id,
    p.amount AS total_amount,
    p.description,
    p.is_repayment,
    p.payment_timestamp,
    pr.from_user_in_room_id,
    pr.to_user_in_room_id,
    pr.amount AS user_amount
FROM payment p
LEFT JOIN payment_record pr ON p.payment_id = pr.payment_id;

-- return balances to be paid
CREATE MATERIALIZED VIEW room_pair_balances_to_pay AS
WITH pair_balance AS (
    SELECT
        p.room_id,
        pr.from_user_in_room_id AS from_user,
        pr.to_user_in_room_id AS to_user,
        SUM(pr.amount) AS balance
    FROM payment_record pr
    JOIN payment p on pr.payment_id = p.payment_id
    -- TODO: manage this view on application when removing user id mapping and getting user in room id
    -- can omit group by room on assumption uuids are unique on the database so wont collide
    -- but select statement give error
    GROUP BY p.room_id, from_user, to_user
)
SELECT * FROM pair_balance;

--CREATE INDEX idx_room_pair_balances_room ON room_pair_balances_to_pay(room_id);
--CREATE INDEX idx_room_pair_balances_users ON room_pair_balances_to_pay(from_user, to_user);
