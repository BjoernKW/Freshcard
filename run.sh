#!/bin/sh

export AWS_ACCESS_KEY_ID=
export AWS_SECRET_ACCESS_KEY=
export S3_BUCKET_NAME=
export S3_ENDPOINT=https://s3.amazonaws.com/
export ALGOLIASEARCH_API_KEY=
export ALGOLIASEARCH_API_KEY_SEARCH=
export ALGOLIASEARCH_APPLICATION_ID=
export ALGOLIASEARCH_INDEX=
export DATABASE_URL=

mvn -Dspring.profiles.active=development jetty:run
