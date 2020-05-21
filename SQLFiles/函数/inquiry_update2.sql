create function inquiry_update2() returns trigger
    language plpgsql
as
$$
declare
    train_n  varchar;
    type     varchar;
    danjia   double precision;
    leave    varchar;
    time     varchar;
    test     double precision;
    lasttest double precision;
begin

    select train_num, train_type
    into train_n,type
    from train
    where train.train_id = new.train_id;
    if new.stop_num = 1 then
        return 0.00;
    end if;
    case type
        when '普快' then danjia = 9.95;
        when '特快' then danjia = 12.05;
        when '高铁' then danjia = 60.30;
        when '直达' then danjia = 11.36;
        when '城际' then danjia = 57.47;
        when '动车' then danjia = 51.29;
        when '其它' then danjia = 4.47;
        end case;

    select i.depart_time into leave from schedule i where i.train_id = new.train_id and i.stop_num = 1;
    time = subtract_time_train(leave, new.arrive_time, train_n, 1, new.stop_num);


    test := danjia * (cast((trim(split_part(time, '小', 1))) as double precision)
        + cast(trim(split_part(split_part(time, '时', 2), '分', 1)) as double precision) / 60.0);
    select price_from_start_station
    into lasttest
    from inquire_table s
    where s.train_num = train_n
      and stop_num = new.stop_num - 1;
    if test < 0 or test < lasttest
    then
        test = test + danjia * 24;
    end if;

    update inquire_table i
    set train_num=train_n,
        stop_num=new.stop_num,
        station_name=s.station_name,
        arrive_time=new.arrive_time,
        depart_time=new.depart_time,
        train_type=round(cast(test as numeric), 2),
        price_from_start_station=test
    from station s
    where i.schedule_id = new.schedule_id
      and s.station_id = new.station_id;
    return new;
end;
$$;

