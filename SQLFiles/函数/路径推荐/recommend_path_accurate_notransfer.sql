--接入别的函数 jdbc不用调用
create function recommend_path_accurate_notransfer(start_station character varying, arrive_station character varying) returns SETOF path_rough
    language plpgsql
as
$$
declare
    tmpv varchar;
    tmpi int;
    tmpd double precision;
begin
    tmpv = '';
    tmpi = 0;
    tmpd = 0.0;
    return query (select t1.train_num                                     as first_train_num,
                         t1.train_type                                    as first_train_type,
                         t1.station_name                                  as first_from,
                         t2.station_name                                  as first_to,
                         t1.depart_time                                   as first_depart,
                         t2.arrive_time                                   as first_arrive,
                         t1.stop_num,
                         t2.stop_num,
                         cast(round(cast(0.8 * (t2.price_from_start_station - t1.price_from_start_station) as
                                        numeric), 2) as double precision),
                         tmpv,
                         tmpv,
                         tmpv,
                         tmpv,
                         tmpv,
                         tmpi,
                         tmpi,
                         tmpd,
                         subtract_time_train(t1.depart_time, t2.arrive_time, t1.train_num,
                                             t1.stop_num, t2.stop_num)    as total_time,
                         cast(round(cast(0.8 * (t2.price_from_start_station - t1.price_from_start_station) as
                                        numeric), 2) as double precision) as total_price
                  from inquire_table t1
                           join inquire_table t2 on t1.train_num = t2.train_num and t1.stop_num < t2.stop_num
                  where t1.station_name = start_station
                    and t2.station_name = arrive_station
    );
end ;
$$;

