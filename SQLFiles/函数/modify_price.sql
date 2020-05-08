create function modify_price(train_n character varying, stop integer, train_type character varying,
                             arrive character varying) returns double precision
    language plpgsql
as
$$
declare
    danjia   double precision;
    leave    varchar;
    time     varchar;
    test     double precision;
    lasttest double precision;
begin
    if stop = 1 then
        return 0.00;
    end if;
    case train_type
        when '普快' then danjia = 9.95;
        when '特快' then danjia = 12.05;
        when '高铁' then danjia = 60.30;
        when '直达' then danjia = 11.36;
        when '城际' then danjia = 57.47;
        when '动车' then danjia = 51.29;
        when '其它' then danjia = 4.47;
        end case;
    select i.depart_time into leave from inquire_table i where i.train_num = train_n and i.stop_num = 1;
    time = subtract_time_train(leave, arrive, train_n, 1, stop);
    test := danjia * (cast((trim(split_part(time, '小', 1))) as double precision)
        + cast(trim(split_part(split_part(time, '时', 2), '分', 1)) as double precision) / 60.0);
    select price_from_start_station into lasttest from inquire_table where train_num = train_n and stop_num = stop - 1;
    if test < 0 or test < lasttest
    then
        test = test + danjia * 24;
    end if;
    return round(cast(test as numeric), 2);
end;
$$;

alter function modify_price(varchar, integer, varchar, varchar) owner to checker;

