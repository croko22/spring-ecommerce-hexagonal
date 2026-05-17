-- Fix image_url column type (was bytea causing lower() errors)
ALTER TABLE products ALTER COLUMN image_url TYPE VARCHAR(255);