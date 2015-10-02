CREATE INDEX
   ON authorities (organization_id ASC NULLS LAST);
CREATE INDEX
   ON authorities (username ASC NULLS LAST);
CREATE INDEX
   ON contacts_users (contact_id ASC NULLS LAST);
CREATE INDEX
   ON contacts_users (user_id ASC NULLS LAST);
CREATE INDEX
   ON contacts_users (organization_id, user_id, contact_id);
CREATE INDEX
   ON contacts_users (organization_id ASC NULLS LAST);
CREATE INDEX
   ON organizations_users (user_id ASC NULLS LAST);
CREATE INDEX
   ON organizations_users (organization_id ASC NULLS LAST);
