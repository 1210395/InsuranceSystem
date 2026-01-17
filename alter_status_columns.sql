-- Alter status columns to allow longer status names
ALTER TABLE healthcare_provider_claims ALTER COLUMN status TYPE VARCHAR(50);
ALTER TABLE claims ALTER COLUMN status TYPE VARCHAR(50);
