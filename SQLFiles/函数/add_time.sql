create function add_time(time1 character varying, time2 character varying) returns character varying
    language plpgsql
as
$$
declare
    result_hour   int;
    result_minute int;
begin
    result_hour := cast(trim(split_part(time1, '小', 1)) as int) + cast(trim(split_part(time2, '小', 1)) as int);
    result_minute := cast(trim(split_part(split_part(time1, '时', 2), '分', 1)) as int)
        + cast(trim(split_part(split_part(time2, '时', 2), '分', 1)) as int);
    if result_minute >= 60 then
        result_minute := result_minute - 60;
        result_hour := result_hour + 1;
    end if;
    return result_hour || ' 小时 ' || result_minute || ' 分钟';
end;
$$;

alter function add_time(varchar, varchar) owner to checker;

