create function subtract_time_train(leave character varying, arrive character varying, tra_n character varying, stop1 integer, stop2 integer) returns character varying
    language plpgsql
as
$$
declare
    hour1         int;
    hour2         int;
    minute1       int;
    minute2       int;
    result_hour   int;
    result_minute int;
    try1          varchar;
    try2          varchar;
begin
    hour1 = cast(substr(leave, 1, 2) as int);
    hour2 = cast(substr(arrive, 1, 2) as int);
    minute1 = cast(substr(leave, 4, 2) as int);
    minute2 = cast(substr(arrive, 4, 2) as int);
    result_hour := hour2 - hour1;
    for i in stop1..stop2 - 1
        loop
            select depart_time into try1 from inquire_table where train_num = tra_n and stop_num = i;
            if i != stop2 - 1
            then
                select depart_time into try2 from inquire_table where train_num = tra_n and stop_num = i + 1;
                if try1 is null or try2 is null
                then
                    continue;
                end if;
                if cast(substr(try1, 1, 2) as int) > cast(substr(try2, 1, 2) as int)
                then
                    result_hour = result_hour + 24;
                end if;
            else
                if cast(substr(try1, 1, 2) as int) > hour2
                then
                    result_hour = result_hour + 24;
                end if;
            end if;
        end loop;
    result_minute := minute2 - minute1;
    if result_minute < 0 then
        result_minute := result_minute + 60;
        result_hour := result_hour - 1;
    end if;
    while result_hour < 0
        loop
            result_hour = result_hour + 24;
        end loop;
    return result_hour || ' 小时 ' || result_minute || ' 分钟';
end;
$$;

alter function subtract_time_train(varchar, varchar, varchar, int4, int4) owner to checker;

