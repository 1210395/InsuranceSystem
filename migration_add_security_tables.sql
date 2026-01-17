-- ========================================
-- Security Enhancement Migration Script
-- Date: 2026-01-02
-- Description: Adds password reset and token revocation tables
-- ========================================

-- =====================================
-- 1. Password Reset Token Table
-- =====================================
CREATE TABLE IF NOT EXISTS password_reset_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

-- Index for faster token lookup
CREATE INDEX IF NOT EXISTS idx_password_reset_token
    ON password_reset_token(token);

-- Index for faster user lookup
CREATE INDEX IF NOT EXISTS idx_password_reset_user
    ON password_reset_token(user_id);

-- Index for expiry date queries
CREATE INDEX IF NOT EXISTS idx_password_reset_expiry
    ON password_reset_token(expiry_date);

-- =====================================
-- 2. Revoked Token Table (JWT Blacklist)
-- =====================================
CREATE TABLE IF NOT EXISTS revoked_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(1024) NOT NULL UNIQUE,
    revoked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_date TIMESTAMP NOT NULL,
    user_id UUID,
    reason VARCHAR(255),
    CONSTRAINT fk_revoked_token_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE SET NULL
);

-- Index for faster token lookup
CREATE INDEX IF NOT EXISTS idx_revoked_token
    ON revoked_token(token);

-- Index for expiry date cleanup
CREATE INDEX IF NOT EXISTS idx_revoked_token_expiry
    ON revoked_token(expiry_date);

-- Index for user lookup
CREATE INDEX IF NOT EXISTS idx_revoked_token_user
    ON revoked_token(user_id);

-- =====================================
-- 3. Cleanup Procedure for Expired Tokens
-- =====================================

-- Function to clean up expired password reset tokens
CREATE OR REPLACE FUNCTION cleanup_expired_password_reset_tokens()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM password_reset_token
    WHERE expiry_date < CURRENT_TIMESTAMP;

    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Function to clean up expired revoked tokens
CREATE OR REPLACE FUNCTION cleanup_expired_revoked_tokens()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM revoked_token
    WHERE expiry_date < CURRENT_TIMESTAMP;

    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- =====================================
-- 4. Add Comments for Documentation
-- =====================================

COMMENT ON TABLE password_reset_token IS
    'Stores password reset tokens with expiry dates';

COMMENT ON COLUMN password_reset_token.token IS
    'Unique token sent to user for password reset';

COMMENT ON COLUMN password_reset_token.expiry_date IS
    'Token expiration timestamp (typically 1-24 hours)';

COMMENT ON TABLE revoked_token IS
    'JWT token blacklist for logged out or invalidated sessions';

COMMENT ON COLUMN revoked_token.token IS
    'Full JWT token string that has been revoked';

COMMENT ON COLUMN revoked_token.expiry_date IS
    'Original JWT expiry date for cleanup purposes';

COMMENT ON COLUMN revoked_token.reason IS
    'Reason for revocation (logout, security breach, etc.)';

-- =====================================
-- 5. Verification Queries
-- =====================================

-- Verify tables were created
DO $$
BEGIN
    IF EXISTS (
        SELECT FROM information_schema.tables
        WHERE table_name = 'password_reset_token'
    ) THEN
        RAISE NOTICE 'Table password_reset_token created successfully';
    ELSE
        RAISE WARNING 'Table password_reset_token was not created';
    END IF;

    IF EXISTS (
        SELECT FROM information_schema.tables
        WHERE table_name = 'revoked_token'
    ) THEN
        RAISE NOTICE 'Table revoked_token created successfully';
    ELSE
        RAISE WARNING 'Table revoked_token was not created';
    END IF;
END $$;

-- =====================================
-- 6. Optional: Add Indexes on Existing Tables
-- =====================================

-- Add index on users email if not exists (for faster password reset lookup)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Add index on users username if not exists (for faster login)
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- =====================================
-- 7. Grant Permissions (Adjust as needed)
-- =====================================

-- Example: Grant permissions to application user
-- GRANT SELECT, INSERT, UPDATE, DELETE ON password_reset_token TO insurance_app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON revoked_token TO insurance_app_user;

-- ========================================
-- Migration Complete
-- ========================================

-- Run cleanup functions manually if needed:
-- SELECT cleanup_expired_password_reset_tokens();
-- SELECT cleanup_expired_revoked_tokens();

-- To schedule automatic cleanup, use pg_cron or application-level scheduler
