--模糊检索 试了很多次 貌似只能这样比较复杂的写 然后这个模糊检索是包括精确检索的内容的 检索时间比精确检索长不少 我这边大概两三秒
--可以提供客户精确检索 和模糊检索两个选项 每个选项下面对应 推荐路径 最短时间 最少费用 最少换乘等等
create function recommend_path_rough(start_station character varying, arrive_station character varying) returns SETOF path_recommend
    language plpgsql
as
$$
declare
    name1 varchar;
    name2 varchar;
begin
    if substr(start_station, length(start_station), 1) = '北' or
       substr(start_station, length(start_station), 1) = '南' or
       substr(start_station, length(start_station), 1) = '东' or
       substr(start_station, length(start_station), 1) = '西' then
        name1 = substr(start_station, 1, length(start_station) - 1);
    else
        name1 = start_station;
    end if;

    if substr(arrive_station, length(arrive_station), 1) = '北' or
       substr(arrive_station, length(arrive_station), 1) = '南' or
       substr(arrive_station, length(arrive_station), 1) = '东' or
       substr(arrive_station, length(arrive_station), 1) = '西' then
        name2 = substr(arrive_station, 1, length(arrive_station) - 1);
    else
        name2 = arrive_station;
    end if;

    return query (select *
                  from recommend_path_accurate(name1, name2)
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '东', name2)
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '南', name2)
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '西', name2)
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '北', name2)
                  union
                  select *
                  from recommend_path_accurate(name1, name2 || '东')
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '东', name2 || '东')
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '南', name2 || '东')
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '西', name2 || '东')
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '北', name2 || '东')
                  union
                  select *
                  from recommend_path_accurate(name1, name2 || '西')
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '东', name2 || '西')
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '南', name2 || '西')
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '西', name2 || '西')
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '北', name2 || '西')
                  union
                  select *
                  from recommend_path_accurate(name1, name2 || '南')
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '东', name2 || '南')
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '南', name2 || '南')
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '西', name2 || '南')
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '北', name2 || '南')
                  union
                  select *
                  from recommend_path_accurate(name1, name2 || '南')
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '东', name2 || '北')
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '南', name2 || '北')
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '西', name2 || '北')
                  union
                  select *
                  from
                      recommend_path_accurate(name1 || '北', name2 || '北')
    );
end;
$$;