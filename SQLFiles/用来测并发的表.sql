create table orders2(
    order_id serial primary key,
    user_name varchar(255),
    person_id varchar(18),
    train_num varchar(10),
    start_station varchar(10),
    depart_time varchar(10),
    arrive_station varchar(10),
    arrive_time varchar(10),
    order_date date,
    seat_type_id int
);