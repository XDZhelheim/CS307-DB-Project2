package Project2_12306;

public class TrainQuery {
	private String train_num, depart_station, arrive_station, depart_time, arrive_time, train_type;

	public TrainQuery(String train_num, String depart_station, String arrive_station, String depart_time,
			String arrive_time, String train_type) {
		this.train_num = train_num;
		this.depart_station = depart_station;
		this.arrive_station = arrive_station;
		this.depart_time = depart_time;
		this.arrive_time = arrive_time;
		this.train_type = train_type;
	}

	public String getTrain_num() {
		return train_num;
	}

	public String getDepart_station() {
		return depart_station;
	}

	public String getArrive_station() {
		return arrive_station;
	}

	public String getDepart_time() {
		return depart_time;
	}

	public String getArrive_time() {
		return arrive_time;
	}

	public String getTrain_type() {
		return train_type;
	}

	@Override
	public String toString() {
		return train_type+train_num+"\t出发站: "+depart_station+"\t到达站: "+arrive_station+"\t出发时间: "+depart_time+"\t到达时间: "+arrive_time;
	}
}
