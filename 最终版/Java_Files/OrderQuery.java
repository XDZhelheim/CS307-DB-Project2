package Project2_12306;

public class OrderQuery {
	private int order_id;
	private String train_num, start_station, arrive_station, person_id;
	private double price;
	private String date, depart_time, arrive_time, seat_type;
	private int date_change;
	
	public OrderQuery(int order_id, String train_num, String start_station, String arrive_station, 
			String person_id, double price, String date, String seat_type, String depart_time, String arrive_time, int date_change) {
		this.order_id = order_id;
		this.train_num = train_num;
		this.start_station = start_station;
		this.arrive_station = arrive_station;
		this.person_id = person_id;
		this.price = price;
		this.date=date;
		this.seat_type=seat_type;
		this.depart_time=depart_time;
		this.arrive_time=arrive_time;
		this.date_change=date_change;
	}

	public int getOrder_id() {
		return order_id;
	}

	public String getTrain_num() {
		return train_num;
	}

	public String getStart_station() {
		return start_station;
	}

	public String getArrive_station() {
		return arrive_station;
	}

	public String getPerson_id() {
		return person_id;
	}

	public double getPrice() {
		return price;
	}

	public String getDate() {
		return date;
	}

	public String getArrive_time() {
		return arrive_time;
	}

	public String getSeatType() {
		return seat_type;
	}

	public int getDate_change() {
		return date_change;
	}

	public String getDepart_time() {
		return depart_time;
	}

	@Override
	public String toString() {
		String s="车次: "+train_num+"\t日期: "+date+" \t出发: "+start_station+"  "+depart_time+"\t到达: "+arrive_station+"  "+arrive_time;
		if (date_change>0)
			s+="(+"+date_change+")";
		s+="\t乘客ID: "+person_id+"\t座位类型: "+seat_type+"\t票价: "+price;
		return s;
	}
	
}
