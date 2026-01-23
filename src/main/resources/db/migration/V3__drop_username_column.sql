-- Migration: Drop username column from clients table
-- The system now uses email for authentication and identification

-- Drop the username column from clients table
ALTER TABLE clients DROP COLUMN IF EXISTS username;

-- Note: Make sure all client lookups use email instead of username
