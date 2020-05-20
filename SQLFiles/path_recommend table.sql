
create table path_recommend
(
    first_train_num    varchar,
    first_train_type   character varying,
    first_from_name    character varying,--出发站的名字
    first_to_name      character varying,  --到达站的名字
    first_from_stop    int,		--出发站的序号 用于方便订票时传参减少余票数 可以不用展现给用户 
    first_to_stop      int,		--到达站的序号
    first_depart       character varying,	--出发的时间
    first_arrive       character varying,       --到达的时间
    first_seat1        int,		--1“一等座”的余票
    first_price1       double precision,	--1“一等座”的价格
    first_seat2        int,		--2“二等座”
    first_price2       double precision,
    first_seat3        int,		--3“硬卧“
    first_price3       double precision,
    first_seat4        int,		--4“软卧“
    first_price4       double precision,
    second_train_num   varchar,	--换乘后的第二趟列车 如果返回字符串为''则表示不需换乘
    second_train_type  character varying,
    second_to_name     character varying,
    second_from_stop   int,
    second_to_stop     int,
    second_leave       character varying,
    second_arrive      character varying,
    second_seat1       int,
    second_price1      double precision,
    second_seat2       int,
    second_price2      double precision,
    second_seat3       int,
    second_price3      double precision,
    second_seat4       int,
    second_price4      double precision,
    total_time         character varying,
    total_lowest_price double precision  --总旅程二等座的价格
);
