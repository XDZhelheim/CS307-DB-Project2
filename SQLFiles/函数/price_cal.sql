create function price_cal() returns trigger
    language plpgsql
as
$$
declare
    tra_id      int;
    stop_number int;
    train_n     varchar;
    type        varchar;
    danjia      double precision;
    leave       varchar;
    arrive      varchar;
    time        varchar;
    test        double precision;
    lasttest    double precision;
    zuowei      double precision;
begin
    select train_id, stop_num, arrive_time
    into tra_id,stop_number,arrive
    from schedule
    where schedule.schedule_id = new.schedule_id;

    if tra_id isnull then raise exception 'tra_id null'; end if;
    select train_num, train_type
    into train_n,type
    from train
             join schedule on train.train_id = schedule.train_id
    where train.train_id = tra_id;
    if type isnull then raise exception 'type null'; end if;
    if stop_number = 1 then
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

    case new.seat_type
        when 1 then zuowei = 1.0;
        when 2 then zuowei = 0.8;
        when 3 then zuowei = 1.4;
        when 4 then zuowei = 1.6;
        end case;

    select i.depart_time into leave from schedule i where i.train_id = tra_id and i.stop_num = 1;
    time = subtract_time_train(leave, arrive, train_n, 1, stop_number);
    test := zuowei * danjia * (cast((trim(split_part(time, '小', 1))) as double precision)
        + cast(trim(split_part(split_part(time, '时', 2), '分', 1)) as double precision) / 60.0);


    select price_from_start_station
    into lasttest
    from detail_schedule d
             join schedule s on d.schedule_id = s.schedule_id
    where s.train_id = tra_id
      and stop_num = stop_number - 1
      and d.seat_type_id = new.seat_type;

    if test < 0 or test < lasttest
    then
        test = test + danjia * 24;
    end if;
    new.price_from_start_station = round(cast(test as numeric), 2);
    return new;
end;
$$;

