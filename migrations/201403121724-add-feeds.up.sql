CREATE TABLE feeds (
 id serial primary key,
 account_id integer not null references accounts(id),
 feed_id uuid not null,
 filename varchar(50) not null);
