CREATE TABLE feeds (
 feed_id uuid primary key,
 user_id uuid not null references users(user_id),
 filename varchar(50) not null);
