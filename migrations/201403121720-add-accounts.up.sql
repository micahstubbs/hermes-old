CREATE TABLE accounts (
  account_id uuid primary key,
  name varchar(100) not null,
  active boolean default true,
  location varchar(100) not null,
  contact_email varchar(100) not null,
  created_at timestamp default now());
