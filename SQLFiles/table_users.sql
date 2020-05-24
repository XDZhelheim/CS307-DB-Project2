create table users (
    id serial primary key,
    user_name varchar(255) not null,
    password varchar(20) not null,
    type char not null,
    unique (user_name),
    check (type in('A', 'P'))
);

alter table users drop column password;

alter table users add column password int;

update users set password=96426 where user_name='adm';

update users set password=97 where user_name='a';
