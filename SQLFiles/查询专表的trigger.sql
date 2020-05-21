--先建立inquiry_update1()  2 3
--insert
create trigger inquiry_update_trigger
    after insert
    on schedule
    for each row
execute procedure inquiry_update1();

--update
create trigger inquiry_update_trigger2
    after update
    on schedule
    for each row
execute procedure inquiry_update2();

--delete
create trigger inquiry_update_trigger3
    before delete
    on schedule
    for each row
execute procedure inquiry_update3();




--测试样例 这个可以用为演示添加车程中的站点
--先检查一下下列搜索搜为空
select *
from schedule
where train_id = 2091
  and stop_num = 14;
--不为空的话执行下列删除语句
delete
from rest_seat
where price_id = 48728
   or price_id = 97298
   or price_id = 145773
   or price_id = 194364;

delete
from price
where schedule_id = 48691;

delete
from schedule
where train_id = 2091
  and stop_num = 14;

delete
from inquire_table
where train_num = 'K421'
  and stop_num = 14;
--看一下查询表里无stop_number=14
select *
from inquire_table i
         join train t on i.train_num = t.train_num
where i.train_num = 'K421';
--尝试插入
insert into schedule
values (14, '23:00:00', '23:15:00', 2091, 190);




