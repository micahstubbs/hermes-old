# hermes

Shuttles VIP feed data from the sources to Metis.

## Setup

To run locally, you'll need postgesql installed, a user called 'hermes-dev', and databases called hermes-dev and hermes-test.

You can set up your dev database in two ways, via the REPL or manually. To do so via the REPL, run `lein repl`, compile and load the hermes.db namespace, and then call (run-migrations!). You can also run (reset-db!) in the same namespace to rollback to a clean database.

If you are unfamilar with the REPL, you can run the migrations manually by running all the migration up sql files found in the migrations directory.

`psql -d hermes-dev -U hermes-dev -a -f migrations/201403121720-add-accounts.up.sql`
`psql -d hermes-dev -U hermes-dev -a -f migrations/201403121724-add-feeds.up.sql`
...

## Usage

To start the server, once the dev database is set up, run:

`lein run-dev`

This will make the server available at localhost:8080.

From there, you can create a feed with:
http://localhost:8080/upload

List feeds:
http://localhost:8080/feeds

Show feed:
http://localhost:8080/feeds/ID (UUID)

Download feed file:
http://localhost:8080/feeds/ID/download
