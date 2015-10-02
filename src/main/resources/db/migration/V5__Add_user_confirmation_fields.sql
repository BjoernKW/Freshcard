ALTER TABLE users
  ADD COLUMN confirmed boolean NOT NULL DEFAULT false;
ALTER TABLE users
  ADD COLUMN preferred_language character varying;
ALTER TABLE users
  ADD COLUMN confirmation_hash_code character varying;
