-- =====================================================
-- Global Policy System Migration
-- Version: 1.0
-- Description: Creates tables for the new global policy management system
-- =====================================================

-- 1. Create service_categories table (Admin can add/edit service categories)
CREATE TABLE IF NOT EXISTS service_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    name_ar VARCHAR(100),
    description TEXT,
    icon VARCHAR(50),
    color VARCHAR(7),
    is_active BOOLEAN DEFAULT TRUE,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. Create global_policy table (Single active policy at a time)
CREATE TABLE IF NOT EXISTS global_policy (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    version VARCHAR(20) NOT NULL,
    description TEXT,
    effective_from DATE NOT NULL,
    effective_to DATE,
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('ACTIVE', 'DRAFT', 'EXPIRED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_by UUID REFERENCES clients(id)
);

-- 3. Create client_limits table (Usage limits for all clients)
CREATE TABLE IF NOT EXISTS client_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_id UUID REFERENCES global_policy(id) ON DELETE CASCADE,

    -- Visit Limits
    max_visits_per_month INTEGER,
    max_visits_per_year INTEGER,

    -- Spending Limits
    max_spending_per_month DECIMAL(12,2),
    max_spending_per_year DECIMAL(12,2),

    -- Deductibles
    annual_deductible DECIMAL(12,2) DEFAULT 0,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    UNIQUE(policy_id)
);

-- 4. Create service_coverage table (Coverage rules per service)
CREATE TABLE IF NOT EXISTS service_coverage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_id UUID REFERENCES global_policy(id) ON DELETE CASCADE,
    category_id UUID REFERENCES service_categories(id),

    -- Service Identification
    service_name VARCHAR(160) NOT NULL,
    medical_name VARCHAR(200),
    description TEXT,

    -- Coverage Rules
    coverage_status VARCHAR(20) DEFAULT 'COVERED' CHECK (coverage_status IN ('COVERED', 'PARTIAL', 'NOT_COVERED')),
    coverage_percent DECIMAL(5,2) DEFAULT 100.00,
    standard_price DECIMAL(12,2) NOT NULL,
    max_coverage_amount DECIMAL(12,2),

    -- Restrictions
    min_age INTEGER,
    max_age INTEGER,
    allowed_gender VARCHAR(10) DEFAULT 'ALL' CHECK (allowed_gender IN ('MALE', 'FEMALE', 'ALL')),
    requires_referral BOOLEAN DEFAULT FALSE,

    -- Usage Limits
    frequency_limit INTEGER,
    frequency_period VARCHAR(20) CHECK (frequency_period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),

    -- Metadata
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    UNIQUE(policy_id, service_name)
);

-- 5. Create category_limits table (Limits per category)
CREATE TABLE IF NOT EXISTS category_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_id UUID REFERENCES global_policy(id) ON DELETE CASCADE,
    category_id UUID REFERENCES service_categories(id),

    max_visits_per_month INTEGER,
    max_visits_per_year INTEGER,
    max_spending_per_month DECIMAL(12,2),
    max_spending_per_year DECIMAL(12,2),

    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    UNIQUE(policy_id, category_id)
);

-- 6. Create client_usage table (Track client usage per month)
CREATE TABLE IF NOT EXISTS client_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID REFERENCES clients(id) ON DELETE CASCADE,

    -- Period tracking
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,

    -- Aggregate usage
    total_visits INTEGER DEFAULT 0,
    total_spending DECIMAL(12,2) DEFAULT 0,

    -- Timestamps
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    UNIQUE(client_id, year, month)
);

-- 7. Create client_service_usage table (Track per-service usage)
CREATE TABLE IF NOT EXISTS client_service_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID REFERENCES clients(id) ON DELETE CASCADE,
    service_coverage_id UUID REFERENCES service_coverage(id) ON DELETE CASCADE,
    category_id UUID REFERENCES service_categories(id),

    year INTEGER NOT NULL,
    month INTEGER,

    usage_count INTEGER DEFAULT 0,
    amount_used DECIMAL(12,2) DEFAULT 0,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    UNIQUE(client_id, service_coverage_id, year, month)
);

-- 8. Create policy_versions table (Audit trail)
CREATE TABLE IF NOT EXISTS policy_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_id UUID REFERENCES global_policy(id) ON DELETE CASCADE,
    version VARCHAR(20) NOT NULL,
    snapshot JSONB NOT NULL,
    changed_by UUID REFERENCES clients(id),
    change_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- =====================================================
-- Indexes for better performance
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_service_coverage_policy ON service_coverage(policy_id);
CREATE INDEX IF NOT EXISTS idx_service_coverage_category ON service_coverage(category_id);
CREATE INDEX IF NOT EXISTS idx_service_coverage_name ON service_coverage(service_name);
CREATE INDEX IF NOT EXISTS idx_category_limits_policy ON category_limits(policy_id);
CREATE INDEX IF NOT EXISTS idx_category_limits_category ON category_limits(category_id);
CREATE INDEX IF NOT EXISTS idx_client_usage_client ON client_usage(client_id);
CREATE INDEX IF NOT EXISTS idx_client_usage_period ON client_usage(year, month);
CREATE INDEX IF NOT EXISTS idx_client_service_usage_client ON client_service_usage(client_id);
CREATE INDEX IF NOT EXISTS idx_client_service_usage_service ON client_service_usage(service_coverage_id);
CREATE INDEX IF NOT EXISTS idx_policy_versions_policy ON policy_versions(policy_id);
CREATE INDEX IF NOT EXISTS idx_global_policy_status ON global_policy(status);

-- =====================================================
-- Default service categories (seed data)
-- =====================================================

INSERT INTO service_categories (name, name_ar, icon, color, display_order) VALUES
('CONSULTATION', 'استشارة', 'LocalHospital', '#4CAF50', 1),
('PHARMACY', 'صيدلية', 'LocalPharmacy', '#2196F3', 2),
('LAB', 'مختبر', 'Science', '#9C27B0', 3),
('RADIOLOGY', 'أشعة', 'MonitorHeart', '#FF9800', 4),
('EMERGENCY', 'طوارئ', 'Emergency', '#F44336', 5),
('DENTAL', 'أسنان', 'MedicalServices', '#00BCD4', 6),
('OPTICAL', 'بصريات', 'Visibility', '#607D8B', 7)
ON CONFLICT (name) DO NOTHING;

-- =====================================================
-- Update trigger for updated_at columns
-- =====================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply triggers
DROP TRIGGER IF EXISTS update_global_policy_updated_at ON global_policy;
CREATE TRIGGER update_global_policy_updated_at
    BEFORE UPDATE ON global_policy
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_client_limits_updated_at ON client_limits;
CREATE TRIGGER update_client_limits_updated_at
    BEFORE UPDATE ON client_limits
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_service_coverage_updated_at ON service_coverage;
CREATE TRIGGER update_service_coverage_updated_at
    BEFORE UPDATE ON service_coverage
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_category_limits_updated_at ON category_limits;
CREATE TRIGGER update_category_limits_updated_at
    BEFORE UPDATE ON category_limits
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_service_categories_updated_at ON service_categories;
CREATE TRIGGER update_service_categories_updated_at
    BEFORE UPDATE ON service_categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_client_service_usage_updated_at ON client_service_usage;
CREATE TRIGGER update_client_service_usage_updated_at
    BEFORE UPDATE ON client_service_usage
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
