CREATE TABLE inquire_table
(
    train_num                varchar(5)       DEFAULT NULL,
    stop_num                 int              DEFAULT NULL,
    station_name             varchar(10)      DEFAULT NULL,
    arrive_time              varchar(10)      DEFAULT NULL,
    depart_time              varchar(10)      DEFAULT NULL,
    train_type               varchar(5)       DEFAULT NULL,
    price_from_start_station double precision default null，
    schedule_id              int
);

alter table inquire_table
    add constraint pr_key
        primary key (train_num, stop_num);

insert into inquire_table (select v.train_num,
                                  v.stop_num,
                                  v.station_name,
                                  v.arrive_time,
                                  v.depart_time,
                                  v.train_type,
                                  v.price_from_start_station,
                                  v.schedule_id
                           from vpath v);
--然后视图暂时就没什么用了 但可以先不用删


create table path_rough
(
    first_train_num    varchar,
    first_train_type   character varying,
    first_from         character varying,
    first_to           character varying,
    first_depart       character varying,
    first_arrive       character varying,
    first_from_stop    int,
    first_to_stop      int,
    first_lowest_price double precision
    second_train_num   varchar,
    second_train_type  character varying,
    second_to          character varying,
    second_leave       character varying,
    second_arrive      character varying,
    second_from_stop   int,
    second_to_stop     int,
    total_time         character varying,
    total_lowest_price double precision
);

--删掉之前的这个
create table path_recommend
(
    first_train_num    varchar,
    first_train_type   character varying,
    first_from         character varying,
    first_to           character varying,
    first_depart       character varying,
    first_arrive       character varying,
    first_seat1        int,
    first_price1       double precision,
    first_seat2        int,
    first_price2       double precision,
    first_seat3        int,
    first_price3       double precision,
    first_seat4        int,
    first_price4       double precision,
    second_train_num   varchar,
    second_train_type  character varying,
    second_to          character varying,
    second_leave       character varying,
    second_arrive      character varying,
    second_seat1       int,
    second_price1      double precision,
    second_seat2       int,
    second_price2      double precision,
    second_seat3       int,
    second_price3      double precision,
    second_seat4       int,
    second_price4      double precision,
    total_time         character varying,
    total_lowest_price double precision
);


   