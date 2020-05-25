--管理员更新日期 删掉过去的一天 增加新的一天
create procedure update_date()
as
$$
begin
    update rest_seat
    set date=date + 7,
        rest_ticket=100
    where date = (select min(date) from rest_seat);
end;
$$   language plpgsql;
--运行
call update_date();