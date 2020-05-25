--日志
create table log
(
    time_record timestamp,
    user_name     varchar,
    behaviour   varchar
);

create function log_reserve() returns trigger
as
$$
declare
begin
    if (select extract (epoch from new.time_record- (select max(time_record) from log where user_name = new.user_name)) < 3)
    then
        raise exception 'wrong';
    end if;
    return new;
end;
$$ language plpgsql;

create trigger reserve_trigger
    before insert
    on log
    for each row
execute procedure log_reserve();