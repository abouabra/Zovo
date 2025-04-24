ALTER TABLE users
ALTER column "createdAt" TYPE timestamptz using "createdAt"::timestamptz;