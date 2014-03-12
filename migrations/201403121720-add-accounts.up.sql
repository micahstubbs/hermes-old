CREATE TABLE accounts (
  id serial primary key,
  account_id uuid not null,
  name varchar(100) not null,
  active boolean default true,
  created_at timestamp default now());
