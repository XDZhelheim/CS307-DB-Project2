create function subtract_time_transfer(leave character varying, arrive character varying) returns character varying
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
begin
    hour1 = cast(substr(leave, 1, 2) as int);
    hour2 = cast(substr(arrive, 1, 2) as int);
    minute1 = cast(substr(leave, 4, 2) as int);
    minute2 = cast(substr(arrive, 4, 2) as int);
    result_hour := hour2 - hour1;
    if result_hour < 0 or result_hour = 0 and minute2 - minute1 <= 10 then
        result_hour := result_hour + 24;
    end if;
    result_minute := minute2 - minute1;
    if result_minute < 0 then
        result_minute := result_minute + 60;
        result_hour := result_hour - 1;
    end if;
    return result_hour || ' 小时 ' || result_minute || ' 分钟';
end;
$$;

alter function subtract_time_transfer(varchar, varchar) owner to checker;

