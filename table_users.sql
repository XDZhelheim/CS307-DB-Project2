create table users (
    id serial primary key,
    user_name varchar(255) not null,
    password varchar(20) not null,
    type char not null,
    unique (user_name, password),
    check (type in('A', 'P'))
);

insert into users (user_name, password, type) values ('default_admin', '666666', 'A');