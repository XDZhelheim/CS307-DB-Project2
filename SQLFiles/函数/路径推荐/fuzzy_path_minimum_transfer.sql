--模糊检索 换乘最少
create function fuzzy_path_minimum_transfer(depart_date date, start_station character varying,
                                            arrive_station character varying, page integer) returns SETOF path_recommend
    language plpgsql
as
$$
declare
    tmpv  varchar;
    tmpi  int;
    tmpd  double precision;
    city1 varchar;
    city2 varchar;
    t     path_rough%rowtype;
begin
    select city into city1 from station s where s.station_name = start_station;
    select city into city2 from station s where s.station_name = arrive_station;
    tmpv = '';
    tmpi = 0;
    tmpd = 0.0;
    for t in (select *
              from recommend_path_fuzzy_notransfer(start_station, arrive_station)
              order by cast(trim(split_part(total_time, '小', 1)) as int),
                       cast(trim(split_part(split_part(total_time, '时', 2), '分', 1)) as int)
              limit 10
              offset
              page * 10 - 10)
        loop
            return next (t.first_train_num,
                         t.first_train_type,
                         t.first_from,
                         t.first_to,
                         t.first_from_stop,
                         t.first_to_stop,
                         t.first_depart,
                         t.first_arrive,
                         min_seat(depart_date, t.first_train_num, t.first_from_stop, t.first_to_stop, 1),
                         cast(round(cast((t.first_lowest_price * 1.25) as
                                        numeric), 2) as double precision),
                         min_seat(depart_date, t.first_train_num, t.first_from_stop, t.first_to_stop, 2),
                         cast(round(cast((t.first_lowest_price) as
                                        numeric), 2) as double precision),
                         min_seat(depart_date, t.first_train_num, t.first_from_stop, t.first_to_stop, 3),
                         cast(round(cast((t.first_lowest_price * 1.75) as
                                        numeric), 2) as double precision),
                         min_seat(depart_date, t.first_train_num, t.first_from_stop, t.first_to_stop, 4),
                         cast(round(cast((t.first_lowest_price * 2.0) as
                                        numeric), 2) as double precision),
                         tmpv,
                         tmpv,
                         tmpv,
                         tmpi,
                         tmpi,
                         tmpv,
                         tmpv,
                         tmpi,
                         tmpd,
                         tmpi,
                         tmpd,
                         tmpi,
                         tmpd,
                         tmpi,
                         tmpd,
                         t.total_time,
                         t.total_lowest_price);
        end loop;
end ;
$$;

