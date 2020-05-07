--为了避免掉坑 以下代码务必按顺序执行


create table train
(
    train_num  varchar not null,
    train_type varchar not null
);

create unique index train_num_uindex
    on train (train_num);

insert into train (select distinct s.train_num, s.train_type
                   from schedule s
                   where s.train_num not in (select t.train_num from train t));

alter table train
    add column train_id serial;

alter table train
    add primary key (train_id);


--
create table station
(
    station_name varchar not null
);

create unique index station_name_uindex
    on station (station_name);

insert into station (select distinct station_name
                     from schedule);

alter table station
    add column station_id serial;

alter table station
    add primary key (station_id);

alter table schedule
    drop column cal_price;
alter table schedule
    drop column train_type;

alter table schedule
    add column train_id int;

update schedule s
set train_id =(select distinct t.train_id from train t where t.train_num = s.train_num);

alter table schedule
    add column station_id int;

update schedule s
set station_id =(select distinct t.station_id from station t where t.station_name = s.station_name);

alter table schedule
drop constraint p_key;

alter table schedule
    drop column station_name;

alter table schedule
    drop column train_num;

alter table schedule
    add constraint p_key
        primary key (train_id, stop_num);

alter table schedule
alter column price_from_start_station set not null;
alter table schedule
alter column spear_seat set not null;
alter table schedule
alter column station_id set not null;

--另外，其他函数如果没下载的话可以先别下 都需要大量更新 但是函数接口应该不会有太大变化
