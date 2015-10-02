ALTER TABLE users
  ADD COLUMN temporary_user boolean NOT NULL DEFAULT false;
ALTER TABLE users
  ADD COLUMN custom_signature text;
