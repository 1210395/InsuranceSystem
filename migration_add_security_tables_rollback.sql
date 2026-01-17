-- ========================================
-- Security Enhancement Migration Rollback Script
-- Date: 2026-01-02
-- Description: Rolls back password reset and token revocation tables
-- WARNING: This will delete all password reset and revoked token data
-- ========================================

-- =====================================
-- 1. Drop Functions
-- =====================================

DROP FUNCTION IF EXISTS cleanup_expired_password_reset_tokens();
DROP FUNCTION IF EXISTS cleanup_expired_revoked_tokens();

-- =====================================
-- 2. Drop Tables
-- =====================================

-- Drop revoked_token table (JWT blacklist)
DROP TABLE IF EXISTS revoked_token CASCADE;

-- Drop password_reset_token table
DROP TABLE IF EXISTS password_reset_token CASCADE;

-- =====================================
-- 3. Drop Optional Indexes (if only added by migration)
-- =====================================

-- Only drop these if they were added by the migration
-- and not part of the original schema
-- DROP INDEX IF EXISTS idx_users_email;
-- DROP INDEX IF EXISTS idx_users_username;

-- =====================================
-- 4. Verification
-- =====================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT FROM information_schema.tables
        WHERE table_name = 'password_reset_token'
    ) THEN
        RAISE NOTICE 'Table password_reset_token dropped successfully';
    ELSE
        RAISE WARNING 'Table password_reset_token still exists';
    END IF;

    IF NOT EXISTS (
        SELECT FROM information_schema.tables
        WHERE table_name = 'revoked_token'
    ) THEN
        RAISE NOTICE 'Table revoked_token dropped successfully';
    ELSE
        RAISE WARNING 'Table revoked_token still exists';
    END IF;
END $$;

-- ========================================
-- Rollback Complete
-- ========================================
