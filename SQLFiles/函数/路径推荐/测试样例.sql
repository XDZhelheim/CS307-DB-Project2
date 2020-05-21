--这些测试样例都是挺OK的 距离远的比较能体现N*N搜索的重要性


-----------------------------
--精确检索


--费用最低（其实名字就是解释了）
select *
from accurate_path_lowest_price('2020-06-01', '深圳北', '北京西', 1);
--换乘最少
select *
from accurate_path_minimum_transfer('2020-06-01', '深圳', '济南', 1);
--时间最短
select *
from accurate_path_shortest_time('2020-06-01', '深圳北', '呼和浩特', 1);
--推荐路径
select *
from accurate_path_recommend('2020-06-01', '深圳北', '广州南', 1);

-----------------------------
--模糊检索

--同理
select *
from fuzzy_path_lowest_price('2020-06-01', '福田', '齐齐哈尔', 1);

select *
from fuzzy_path_minimum_transfer('2020-06-01', '福田', '广州', 1);

select *
from fuzzy_path_shortest_time('2020-06-01', '福田', '齐齐哈尔', 1);

select *
from fuzzy_path_recommend('2020-06-01', '福田', '武汉', 1);