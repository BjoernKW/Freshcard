ALTER TABLE contacts_users
  ADD COLUMN updated_offline TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;