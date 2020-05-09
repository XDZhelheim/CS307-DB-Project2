create table orders
(
    order_id serial primary key,
    user_name varchar(255) references users(user_name),
    train_num varchar(5) references train(train_num),
    start_station varchar(10) references station(station_name),
    arrive_station varchar(10) references station(station_name),
    price double precision
);