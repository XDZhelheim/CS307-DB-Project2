--
alter table schedule
    add column schedule_id serial;

alter table schedule
    drop constraint p_key;

alter table schedule
    add primary key (schedule_id);

alter table schedule
    add constraint unique_train_stop unique (train_id, stop_num);

--想起来她上课说可能要检查能否新增一种类型的座位 所以咱们只能另外建表了
--新增列车类型也是有可能的 所以只好按她的走了 前端管理员界面可能得能处理这些..
create table seat_type
(
    type_id   serial
        constraint seat_type_pk
            primary key,
    type_name varchar not null
);

create unique index seat_type_type_name_uindex
    on seat_type (type_name);

insert into seat_type (type_name)
values ('一等座'),
       ('二等座'),
       ('硬卧'),
       ('软卧');

--心痛 下午加的现在又要删了 希望这回是最后一回改表结构了
--先删掉之前的view
create or replace view vpath as
select t.train_num,
       ss.stop_num,
       s.station_name,
       ss.arrive_time,
       ss.depart_time,
       t.train_type
from schedule ss
         inner join train t on ss.train_id = t.train_id
         inner join station s on ss.station_id = s.station_id;


alter table schedule
    drop column price_from_start_station;
alter table schedule
    drop column spear_seat;

drop trigger price_trigger on schedule;

create table detail_schedule
(
    date         date not null,
    schedule_id  int  not null
        constraint detail_schedule_schedule_schedule_id_fk
            references schedule,
    seat_type_id int  not null
        constraint detail_schedule_seat_type_type_id_fk
            references seat_type,
    rest_ticket  int,
    price_from_start_station double precision,
    constraint detail_schedule_pk
        primary key (date, schedule_id, seat_type_id)
);


create function price_cal() returns trigger
as
$$
declare
    tra_id      int;
    stop_number int;
    train_n     varchar;
    type        varchar;
    danjia      double precision;
    leave       varchar;
    arrive      varchar;
    time        varchar;
    test        double precision;
    lasttest    double precision;
    zuowei      double precision;
begin
    select train_id, stop_num, arrive_time
    into tra_id,stop_number,arrive
    from schedule
             join detail_schedule ds on schedule.schedule_id = ds.schedule_id
    where ds.schedule_id = new.schedule_id;
if tra_id isnull then raise exception 'tra_id null';end if;
    select train_num, train_type
    into train_n,type
    from train
             join schedule on train.train_id = schedule.train_id
    where train.train_id = tra_id;
if type isnull then raise exception 'type null';end if;
    if stop_number = 1 then
        return 0.00;
    end if;
    case type
        when '普快' then danjia = 9.95;
        when '特快' then danjia = 12.05;
        when '高铁' then danjia = 60.30;
        when '直达' then danjia = 11.36;
        when '城际' then danjia = 57.47;
        when '动车' then danjia = 51.29;
        when '其它' then danjia = 4.47;
        end case;

    case new.seat_type_id
        when 1 then zuowei = 1.0;
        when 2 then zuowei = 0.8;
        when 3 then zuowei = 1.4;
        when 4 then zuowei = 1.6;
        end case;

    select i.depart_time into leave from schedule i where i.train_id = tra_id and i.stop_num = 1;
    time = subtract_time_train(leave, arrive, train_n, 1, stop_number);
    test := zuowei*danjia * (cast((trim(split_part(time, '小', 1))) as double precision)
        + cast(trim(split_part(split_part(time, '时', 2), '分', 1)) as double precision) / 60.0);


    select price_from_start_station
    into lasttest
    from detail_schedule d
             join schedule s on d.schedule_id = s.schedule_id
    where s.train_id = tra_id
      and stop_num = stop_number - 1
      and d.seat_type_id = new.seat_type_id;

    if test < 0 or test < lasttest
    then
        test = test + danjia * 24;
    end if;
    new.price_from_start_station = round(cast(test as numeric), 2);
    return new;
end;
$$   language plpgsql;



create trigger p_trigger
    before insert
    on detail_schedule
    for each row
execute procedure price_cal();


--然后试一下执行下个语句有没有问题 （exception或price为null）

insert into detail_schedule (date, schedule_id, seat_type_id, rest_ticket) values ('2020-05-27', 1, 1, cast(random() * 100 as int) );


