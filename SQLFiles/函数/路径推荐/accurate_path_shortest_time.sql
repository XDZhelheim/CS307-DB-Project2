--精确检索 时间最短
create function accurate_path_shortest_time(depart_date date, start_station character varying, arrive_station character varying, page integer) returns SETOF path_recommend
    language plpgsql
as
$$
declare
    t   path_rough%rowtype;
    tmp int;
begin

    for t in (select *
              from recommend_path_accurate(start_station, arrive_station)
              order by cast(trim(split_part(total_time, '小', 1)) as int),
                       cast(trim(split_part(split_part(total_time, '时', 2), '分', 1)) as int)
              limit 10
              offset
              page * 10 - 10)
        loop
            select date_change
            into tmp
            from date_change(t.first_train_num, t.first_from_stop, t.first_to_stop);
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
                         t.second_train_num,
                         t.second_train_type,
                         t.second_to,
                         t.second_from_stop,
                         t.second_to_stop,
                         t.second_leave,
                         t.second_arrive,
                         min_seat(depart_date + tmp, t.second_train_num, t.second_from_stop, t.second_to_stop,
                                  1),
                         cast(round(cast((t.total_lowest_price - t.first_lowest_price) * 1.25 as
                                        numeric), 2) as double precision),
                         min_seat(depart_date + tmp, t.second_train_num, t.second_from_stop, t.second_to_stop,
                                  2),
                         cast(round(cast((t.total_lowest_price - t.first_lowest_price) as
                                        numeric), 2) as double precision),
                         min_seat(depart_date + tmp, t.second_train_num, t.second_from_stop, t.second_to_stop,
                                  3),
                         cast(round(cast((t.total_lowest_price - t.first_lowest_price) * 1.75 as
                                        numeric), 2) as double precision),
                         min_seat(depart_date + tmp, t.second_train_num, t.second_from_stop, t.second_to_stop,
                                  4),
                         cast(round(cast((t.total_lowest_price - t.first_lowest_price) * 2.0 as
                                        numeric), 2) as double precision),
                         t.total_time,
                         t.total_lowest_price);
        end loop;

end ;
$$;

