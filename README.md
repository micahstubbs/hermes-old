# hermes

Shuttles VIP feed data from the sources to Metis.

## Usage

Still in early development, no database or user management, and only rudimentary file handling currently.

To run locally, have leiningen installed (2.3+) (brew install leiningen), and use:

`lein run-dev`

This will make the server available at localhost:8080.

You'll need a postgres DB setup, named hermes-dev, with a user named hermes-dev without a password.
Install the following into database:
create sequence feed_id increment by 1 start with 1;
create table feeds (
  id integer PRIMARY KEY DEFAULT nextval('feed_id'),
  feed_id varchar(40) NOT NULL,
  filename varchar(50) NOT NULL
);

From there, you can create a feed with:
http://localhost:8080/upload

List feeds:
http://localhost:8080/feeds

Show feed:
http://localhost:8080/feeds/ID (UUID)

Download feed file:
http://localhost:8080/feeds/ID/download
