ALTER TABLE authorities ADD COLUMN organization_id integer;
ALTER TABLE authorities
  ADD CONSTRAINT authorities_organization_id FOREIGN KEY (organization_id)
      REFERENCES organizations (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE authorities
  ADD CONSTRAINT authorities_authority_username_organization_ui UNIQUE(username, organization_id, authority);
ALTER TABLE authorities DROP CONSTRAINT IF EXISTS authorities_authority_username_ui;
ALTER TABLE authorities DROP CONSTRAINT IF EXISTS authorities_organization_name_fk;

ALTER TABLE organizations DROP CONSTRAINT IF EXISTS organizations_name_uq;
