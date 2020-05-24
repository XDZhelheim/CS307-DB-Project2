--接入别的函数
create function subtract_time(time1 character varying, time2 character varying) returns integer
    language plpgsql
as
$$
begin
    return cast(substr(time1, 1, 2) as int) * 60 + cast(substr(time1, 4, 2) as int) -
           cast(substr(time2, 1, 2) as int) * 60 + cast(substr(time2, 4, 2) as int);
end;
$$;

