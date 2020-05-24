--接触发器 检查订票是否合法 有极端情况没考虑不过也不用管了
create function check_order() returns trigger
    language plpgsql
as
$$
declare
    judge bool;
    t     orders%rowtype;
begin
    judge = true;
    for t in (select * from orders where person_id = new.person_id and order_date = new.order_date)
        loop
            if subtract_time(t.depart_time, new.depart_time) < 0 and
               subtract_time(t.arrive_time, new.depart_time) > -10 and t.arrive_station = new.start_station
                or subtract_time(t.depart_time, new.depart_time) < 0 and t.date_change != 0
                or
               subtract_time(new.depart_time, t.depart_time) < 0 and
               subtract_time(new.arrive_time, t.depart_time) > -10 and t.arrive_station = new.start_station
                or subtract_time(new.depart_time, t.depart_time) < 0 and new.date_change != 0
                or subtract_time(t.depart_time, new.depart_time) < 0 and
                   subtract_time(t.arrive_time, new.depart_time) > -40 and t.arrive_station != new.start_station
                or subtract_time(new.depart_time, t.depart_time) < 0 and
                   subtract_time(new.arrive_time, t.depart_time) > -40 and t.arrive_station != new.start_station
            then
                raise exception 'wrong';
            end if;
        end loop;

    for t in (select *
              from orders
              where person_id = new.person_id
                and order_date = new.order_date - 1
                and date_change >= 1)
        loop
            if subtract_time(t.arrive_time, new.depart_time) > -10 and t.arrive_station = new.start_station
                or t.date_change > 1
                or subtract_time(t.arrive_time, new.depart_time) > -40 and t.arrive_station != new.start_station
            then
                raise exception 'wrong';
            end if;
        end loop;

    for t in (select *
              from orders
              where person_id = new.person_id
                and order_date = new.order_date - 2
                and date_change >= 2)
        loop
            if subtract_time(t.arrive_time, new.depart_time) > -10 and t.arrive_station = new.start_station
                or t.date_change > 2
                or subtract_time(t.arrive_time, new.depart_time) > -40 and t.arrive_station != new.start_station
            then
                raise exception 'wrong';
            end if;
        end loop;

    for t in (select *
              from orders
              where person_id = new.person_id
                and order_date = new.order_date + 1
                and new.date_change >= 1)
        loop
            if subtract_time(new.arrive_time, t.depart_time) > -10 and t.arrive_station = new.start_station
                or new.date_change > 1
                or subtract_time(new.arrive_time, t.depart_time) > -40 and t.arrive_station != new.start_station
            then
                raise exception 'wrong';
            end if;
        end loop;

    for t in (select *
              from orders
              where person_id = new.person_id
                and order_date = new.order_date + 2
                and new.date_change >= 2)
        loop
            if subtract_time(new.arrive_time, t.depart_time) > -10 and t.arrive_station = new.start_station
                or new.date_change > 2
                or subtract_time(new.arrive_time, t.depart_time) > -40 and t.arrive_station != new.start_station
            then
                raise exception 'wrong';
            end if;
        end loop;

    if (select *
        from orders
        where person_id = new.person_id
          and order_date = new.order_date
          and train_num = new.train_num) is not null
    then
        raise exception 'wrong';
    end if;

end;
$$;

