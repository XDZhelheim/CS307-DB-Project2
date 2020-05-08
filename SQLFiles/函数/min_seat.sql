create function min_seat(tra_num varchar, stop1 integer, stop2 integer) returns integer
    language plpgsql
as
$$
begin
    return (select min(spear_seat)
            from (select t.spear_seat
                  from inquire_table t
                  where t.train_num = tra_num
                    and t.stop_num >= stop1
                    and t.stop_num < stop2) as tss);
end;
$$;

alter function min_seat(varchar, integer, integer) owner to checker;

