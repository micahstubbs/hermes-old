CREATE TABLE feeds (
 feed_id uuid primary key,
 account_id uuid not null references accounts(account_id),
 filename varchar(50) not null);
