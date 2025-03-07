-- Drop views and materialized views first, as they depend on tables
DROP MATERIALIZED VIEW IF EXISTS room_pair_balances_to_pay;
DROP VIEW IF EXISTS room_payment_log;

-- Drop tables, ensuring dependent tables are dropped in reverse order of reference
DROP TABLE IF EXISTS payment_record CASCADE;
DROP TABLE IF EXISTS payment CASCADE;
DROP TABLE IF EXISTS user_in_room CASCADE;
DROP TABLE IF EXISTS room CASCADE;
DROP TABLE IF EXISTS "user" CASCADE;
