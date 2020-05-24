----5.7

--这样会返回一个表
select *
from recommend_path('北京西', '深圳北');
--这样会返回一个结果集
select recommend_path('北京西', '深圳北');

--更新price
update train
set price_from_start_station=modify_price(train_num, stop_num, train_type, arrive_time);

--建临时表 用于返回推荐路径
create table path_recommend
(
    first_train_num   varchar,
    first_train_type  character varying,
    first_price       double precision,
    first_from        character varying,
    first_to          character varying,
    first_depart      character varying,
    first_arrive      character varying,
    first_left_seat   int,
    second_train_num  varchar,
    second_train_type character varying,
    second_price      double precision,
    second_to         character varying,
    second_leave      character varying,
    second_arrive     character varying,
    second_left_seat  int,
    total_time        character varying,
    total_price       double precision
);


--5.8

--更新外键 （昨天忘了

alter table schedule
add constraint fk_train_id
foreign key (train_id)
references train (train_id);

alter table schedule
add constraint fk_station_id
foreign key (station_id)
references station (station_id);

--5.9

--经过多次对比 建一个查询专用表有明显效率提升
--另 path_recommend 临时表里的结果是不换乘和一次换乘的union 因为列数必须一样 且不能返回null '' ' '等无关内容 
--所以暂时设定的是不换乘的第二班列车信息和第一班相同 展示结果时只需根据first_to是否为目的地作if判断 

CREATE TABLE inquire_table
(
    train_num                varchar(5)       DEFAULT NULL,
    stop_num                 int              DEFAULT NULL,
    station_name             varchar(10)      DEFAULT NULL,
    arrive_time              varchar(10)      DEFAULT NULL,
    depart_time              varchar(10)      DEFAULT NULL,
    train_type               varchar(5)       DEFAULT NULL,
    price_from_start_station double precision default null,
    spear_seat               int              default null
);

alter table inquire_table
    add constraint pr_key
        primary key (train_num, stop_num);

--插入语句和之前的一样 记得改一下表的名字 插入之后调用 modify_price算票价

update inquire_table
set price_from_start_station=modify_price(train_num, stop_num, train_type, arrive_time);

5.10
--view处理 对比了很多次查询效率 基本上没有差别 inquire_table理论上可以作废
--但是这里不是很懂老师说的更新表时view不会更新，我在网上找到的是view会随表更新而更新，试了一下也是这样

create view vpath as
select t.train_num,
       ss.stop_num,
       s.station_name,
       ss.arrive_time,
       ss.depart_time,
       t.train_type,
       ss.price_from_start_station,
       ss.spear_seat
from schedule ss
         inner join train t on ss.train_id = t.train_id
         inner join station s on ss.station_id = s.station_id;


--5.18
--创建calculate_price函数返回trigger（代码单独放在函数文件夹里
--创建trigger
create trigger price_trigger
    before insert
    on schedule
    for each row
execute procedure calculate_price();

--insert to schedule样例

insert into schedule(stop_num, arrive_time, depart_time,  spear_seat, train_id, station_id)
values (14, '01:00:00', '01:27:00', 1, 2091, 1);

select *
from schedule
where train_id = 2091;

delete
from schedule
where train_id = 2091
  and stop_num = 14;


--modify_price函数暂时没用了 可以删除



create index train_num_index on inquire_table (train_num);
create index schedule_id_index on inquire_table (schedule_id);
create index stop_num_index on inquire_table (stop_num);


--检查订票是否合法
create trigger check_order_trigger
    before insert
    on orders
    for each row
execute procedure check_order();