create function recommend_path_accurate(start_station character varying, arrive_station character varying) returns SETOF path_recommend
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
    return query (select t1.train_num                                              as first_train_num,
                         t1.train_type                                             as first_train_type,
                         round(cast(t2.price_from_start_station - t1.price_from_start_station as
                                   numeric), 2)                                    as first_price,
                         t1.station_name                                           as first_from,
                         t2.station_name                                           as first_to,
                         t1.depart_time                                            as first_depart,
                         t2.arrive_time                                            as first_arrive,
                         min_seat(t1.train_num, t1.stop_num, t2.stop_num)          as first_left_seat,
                         tmpv                                                      as second_train_num,
                         tmpv,
                         tmpd,
                         tmpv,
                         tmpv,
                         tmpv,
                         tmpi,
                         subtract_time_train(t1.depart_time, t2.arrive_time, t1.train_num,
                                             t1.stop_num, t2.stop_num)             as total_time,
                         t2.price_from_start_station - t1.price_from_start_station as total_price
                  from vpath t1
                           join vpath t2 on t1.train_num = t2.train_num and t1.stop_num < t2.stop_num
                  where t1.station_name = start_station
                    and t2.station_name = arrive_station
                  union
                  select t1.train_num                                           as first_train_num,
                         t1.train_type                                          as first_train_type,
                         cast(round(cast(t2.price_from_start_station - t1.price_from_start_station as
                                        numeric), 2) as double precision)       as first_price,
                         t1.station_name                                        as first_from,
                         t2.station_name                                        as first_to,
                         t1.depart_time                                         as first_depart,
                         t2.arrive_time                                         as first_arrive,
                         min_seat(t1.train_num, t1.stop_num, t2.stop_num)       as first_left_seat,
                         t3.train_num                                           as second_train_num,
                         t3.train_type,
                         cast(round(cast(t2.price_from_start_station - t1.price_from_start_station as
                                        numeric), 2) as double precision),
                         t4.station_name,
                         t3.depart_time,
                         t4.arrive_time,
                         min_seat(t3.train_num, t3.stop_num, t4.stop_num),
                         add_time(add_time(subtract_time_train(t3.depart_time, t4.arrive_time, t3.train_num,
                                                               t3.stop_num, t4.stop_num),
                                           subtract_time_train(t1.depart_time, t2.arrive_time, t1.train_num,
                                                               t1.stop_num, t2.stop_num)
                                      ), subtract_time_transfer(t2.arrive_time, t3.depart_time))
                                                                                as total_time,
                         round(cast(t4.price_from_start_station - t3.price_from_start_station +
                                    t2.price_from_start_station -
                                    t1.price_from_start_station as numeric), 2) as total_price
                  from vpath t1
                           join vpath t2 on t1.train_num = t2.train_num and t1.stop_num < t2.stop_num
                           join vpath t3 on t3.station_name = t2.station_name
                           join vpath t4 on t4.train_num = t3.train_num and t3.stop_num < t4.stop_num
                  where t1.station_name = start_station
                    and t4.station_name = arrive_station
    );
end ;
$$;