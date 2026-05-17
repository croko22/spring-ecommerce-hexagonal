-- V10: Fix notifications table id column type (SERIAL -> BIGINT)

-- Drop the old serial and recreate as bigint
ALTER TABLE notifications DROP CONSTRAINT notifications_pkey;
ALTER TABLE notifications ALTER COLUMN id DROP DEFAULT;
ALTER TABLE notifications ALTER COLUMN id TYPE BIGINT;
ALTER TABLE notifications ADD PRIMARY KEY (id);