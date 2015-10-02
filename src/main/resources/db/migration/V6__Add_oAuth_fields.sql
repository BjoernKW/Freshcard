ALTER TABLE users
  ADD COLUMN use_oauth boolean NOT NULL DEFAULT false;
ALTER TABLE users
  ADD COLUMN most_recently_used_oauth_service character varying;
ALTER TABLE users
  ADD COLUMN github_access_token character varying;
ALTER TABLE users
  ADD COLUMN linkedin_access_token character varying;
ALTER TABLE users
  ADD COLUMN twitter_access_token character varying;
ALTER TABLE users
  ADD COLUMN xing_access_token character varying;