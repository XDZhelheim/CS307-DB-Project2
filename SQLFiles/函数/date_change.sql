--用于判断从出发站到终点站跨了几天 返回天数
create function date_change( tra_num character varying, stop1 integer, stop2 integer) returns integer
    language plpgsql
as
$$
declare
    change   int;
    tmp1     varchar;
    tmp2     varchar;
    stoptmp1 int;
    stoptmp2 int;
begin
    change = 0;
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
    if stoptmp1 = stop2 then
        return change;
    end if;
    change = 1;
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
    if stoptmp2 = stop2 then
        return  change;
    end if;
    change = 2;
    return change;

end;
$$;

