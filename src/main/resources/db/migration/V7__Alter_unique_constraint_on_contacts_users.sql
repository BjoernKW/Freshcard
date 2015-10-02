ALTER TABLE contacts_users
  DROP CONSTRAINT contacts_users_uq;
ALTER TABLE contacts_users
  ADD CONSTRAINT contacts_users_uq UNIQUE(contact_id, user_id, organization_id);
