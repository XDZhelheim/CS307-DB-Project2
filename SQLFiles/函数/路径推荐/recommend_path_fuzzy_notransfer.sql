create function recommend_path_fuzzy_notransfer(start_station character varying, arrive_station character varying) returns SETOF path_rough
as
$$
declare
    tmpv  varchar;
    tmpi  int;
    tmpd  double precision;
    city1 varchar;
    city2 varchar;
begin
    tmpv = '';
    tmpi = 0;
    tmpd = 0.0;
    select city into city1 from station s where s.station_name = start_station;
    select city into city2 from station s where s.station_name = arrive_station;
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
                         subtract_time_train(t1.depart_time, t2.arrive_time, t1.train_num,
                                             t1.stop_num, t2.stop_num)    as total_time,
                         cast(round(cast(0.8 * (t2.price_from_start_station - t1.price_from_start_station) as
                                        numeric), 2) as double precision) as total_price
                  from inquire_table t1
                           join inquire_table t2 on t1.train_num = t2.train_num and t1.stop_num < t2.stop_num
                  where t1.station_name in (select station_name from station where city = city1)
                    and t2.station_name in (select station_name from station where city = city2)
    );
end ;
$$;

