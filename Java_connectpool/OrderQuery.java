package Project2_12306;

public class OrderQuery {
	private int order_id;
	private String train_num, start_station, arrive_station, person_id;
	private double price;
	
	public OrderQuery(int order_id, String train_num, String start_station, String arrive_station, String person_id, double price) {
		this.order_id = order_id;
		this.train_num = train_num;
		this.start_station = start_station;
		this.arrive_station = arrive_station;
		this.person_id = person_id;
		this.price = price;
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

	@Override
	public String toString() {
		return "车次: "+train_num+"\t始发站: "+start_station+"\t到达站: "+arrive_station+"\t乘客ID: "+person_id+"\t票价: "+price;
	}
	
}
