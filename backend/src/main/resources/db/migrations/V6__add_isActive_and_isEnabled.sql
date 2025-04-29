-- Add isActive column with a default value and NOT NULL constraint
ALTER TABLE users
    ADD COLUMN "isActive" BOOLEAN NOT NULL DEFAULT FALSE;

-- Add isEnabled column with a default value and NOT NULL constraint
ALTER TABLE users
    ADD COLUMN "isEnabled" BOOLEAN NOT NULL DEFAULT FALSE;
