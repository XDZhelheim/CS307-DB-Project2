--退票的时候把余座加回来

create function addback_seat(dat date, tra_num character varying, stop1 integer, stop2 integer, seat_typ integer) returns void
    language plpgsql
as
$$
declare
    date_change int;
    tmp1        varchar;
    tmp2        varchar;
    stoptmp1    int;
    stoptmp2    int;

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

    update rest_seat
    set rest_ticket=rest_ticket + 1
    where date = (dat + date_change)
      and price_id in
          (select price_id
           from price p
           where p.seat_type = seat_typ
             and schedule_id in
                 (select i.schedule_id
                  from inquire_table i
                  where i.train_num = tra_num
                    and stop_num >= stop1
                    and stop_num < stoptmp1));

    if stoptmp1 = stop2 then
        return;
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

    update rest_seat
    set rest_ticket=rest_ticket + 1
    where date = (dat + date_change)
      and price_id in
          (select price_id
           from price p
           where p.seat_type = seat_typ
             and schedule_id in
                 (select i.schedule_id
                  from inquire_table i
                  where i.train_num = tra_num
                    and stop_num >= stoptmp1
                    and stop_num < stoptmp2));

    if stoptmp2 = stop2 then
        return;
    end if;

    date_change = 2;

    update rest_seat
    set rest_ticket=rest_ticket + 1
    where date = (dat + date_change)
      and price_id in
          (select price_id
           from price p
           where p.seat_type = seat_typ
             and schedule_id in
                 (select i.schedule_id
                  from inquire_table i
                  where i.train_num = tra_num
                    and stop_num >= stoptmp2
                    and stop_num < stop2));
    return;

end;
$$;