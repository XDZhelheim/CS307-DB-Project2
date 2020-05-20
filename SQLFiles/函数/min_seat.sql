--参数分别是 离开出发站的日期， 列车号，出发站的stop_number，到达站的...，座位类型 int
create function min_seat(dat date, tra_num character varying, stop1 integer, stop2 integer, seat_typ integer) returns int
    language plpgsql
as
$$
declare
    date_change int;
    tmp1        varchar;
    tmp2        varchar;
    stoptmp1    int;
    stoptmp2    int;
    min1        int;
    min2        int;
    min3        int;
begin
    date_change = 0;
    stoptmp1 = stop2;
    select depart_time into tmp1 from inquire_table v where v.train_num = tra_num and v.stop_num = stop1;
    for i in stop1 + 1..stop2
        loop
            if i != stop2 then
                select depart_time into tmp2 from inquire_table v where v.train_num = tra_num and v.stop_num = i;
            else
                select arrive_time into tmp2 from inquire_table v where v.train_num = tra_num and v.stop_num = i;
            end if;
            if cast(substr(tmp1, 1, 2) as int) > cast(substr(tmp2, 1, 2) as int)
            then
                stoptmp1 = i;
                exit;
            end if;
            tmp1 = tmp2;
        end loop;

    min1 = (select min(rest_ticket)
            from (select rest_ticket
                  from rest_seat r
                  where r.price_id in (select price_id
                                       from price p
                                       where p.schedule_id in (select t.schedule_id
                                                               from inquire_table t
                                                               where t.train_num = tra_num
                                                                 and t.stop_num >= stop1
                                                                 and t.stop_num < stoptmp1)
                                         and p.seat_type = seat_typ)
                    and r.date = dat) as tss);
    if stoptmp1 = stop2 then
        return min1;
    end if;
    date_change = 1;
    stoptmp2 = stop2;
    select depart_time into tmp1 from inquire_table v where v.train_num = tra_num and v.stop_num = stoptmp1 + 1;
    for i in stoptmp1 + 1..stop2
        loop
            if i != stop2 then
                select depart_time into tmp2 from inquire_table v where v.train_num = tra_num and v.stop_num = i;
            else
                select arrive_time into tmp2 from inquire_table v where v.train_num = tra_num and v.stop_num = i;
            end if;
            if cast(substr(tmp1, 1, 2) as int) > cast(substr(tmp2, 1, 2) as int)
            then
                stoptmp2 = i;
                exit;
            end if;
            tmp1 = tmp2;
        end loop;


    min2 = (select min(rest_ticket)
            from (select rest_ticket
                  from rest_seat r
                  where r.price_id in (select price_id
                                       from price p
                                       where p.schedule_id in (select t.schedule_id
                                                               from inquire_table t
                                                               where t.train_num = tra_num
                                                                 and t.stop_num >= stoptmp1
                                                                 and t.stop_num < stoptmp2)
                                         and p.seat_type = seat_typ)
                    and r.date = dat + 1) as tss);
    if min1 > min2
    then
        min1 = min2;
    end if;
    if stoptmp2 = stop2 then
        return min1;
    end if;
    date_change = 2;

    min3 = (select min(rest_ticket)
            from (select rest_ticket
                  from rest_seat r
                  where r.price_id in (select price_id
                                       from price p
                                       where p.schedule_id in (select t.schedule_id
                                                               from inquire_table t
                                                               where t.train_num = tra_num
                                                                 and t.stop_num >= stoptmp2
                                                                 and t.stop_num < stop2)
                                         and p.seat_type = seat_typ)
                    and r.date = dat + 2) as tss);
    if min1 < min3
    then
        return  min1;
    else
        return min3;
    end if;
end;
$$;

