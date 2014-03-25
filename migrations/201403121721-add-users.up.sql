CREATE TABLE users (
  user_id uuid primary key,
  account_id uuid not null references accounts(account_id),
  api_key uuid not null,
  email varchar(100) not null,
  active boolean not null default true);
