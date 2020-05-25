package Project2_12306;

import java.sql.*;
import java.util.Random;

public class TestConcurrency {
	public static void main(String[] args) throws InterruptedException {
		DBMSThread[] dt=new DBMSThread[200];
		for (int i=0;i<dt.length;i++) 
			dt[i]=new DBMSThread();
		for (int i=0;i<dt.length;i++) 
			dt[i].start();
		Thread.sleep(10000);
		System.out.println("平均用时: "+DBMSThread.timesum/DBMSThread.count+"ms");
	}
}

class DBMSThread extends Thread {
	static Random ran=new Random();
	static long timesum=0;
	static int count=0;
	
	@Override
	public void run() {
		try {
			long timeStart=System.currentTimeMillis();
		  	Connection conn=ProxoolConnectionPool.connector.getConn();
		  	
		  	String train_num="G9999";
		  	String date="2020-06-01";
		  	String start_station="北京";
		  	String arrive_station="深圳北";
		  	String depart_time="08:36:00";
		  	String arrive_time="12:55:00";
		  	int stop1=1;
		  	int stop2=4;
		  	int seat_type_id=1;
	    	
	    	conn.prepareStatement("BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE;").execute();
	    	
	    	String sql="insert into orders2 (user_name, person_id, train_num, start_station, arrive_station, order_date, seat_type_id, depart_time, arrive_time) "
					+ "values (?, ?, ?, ?, ?, cast(? as date), ?, ?, ?);";
			PreparedStatement stmt=conn.prepareStatement(sql);
			stmt.setString(1, this.getName());
			stmt.setString(2, Integer.toString(ran.nextInt(5000)));
			stmt.setString(3, train_num);
			stmt.setString(4, start_station);
			stmt.setString(5, arrive_station);
			stmt.setString(6, date);
			stmt.setInt(7, seat_type_id);
			stmt.setString(8, depart_time);
			stmt.setString(9, arrive_time);
			stmt.execute();
			
			boolean flag=true;
			sql="select subtract_seat(cast(? as date), ?, ?, ?, ?);";
			stmt=conn.prepareStatement(sql);
			stmt.setString(1, date);
			stmt.setString(2, train_num);
			stmt.setInt(3, stop1);
			stmt.setInt(4, stop2);
			stmt.setInt(5, 1);
			try {
				stmt.execute();
			}
			catch (SQLException e) {
				conn.prepareStatement("ROLLBACK;").execute();
				flag=false;
			}
			
			if (flag)
				conn.prepareStatement("COMMIT;").execute();
			long timeEnd=System.currentTimeMillis();
			long timeCost=timeEnd - timeStart;
	    	if (flag) {
	    		System.out.println(this.getName()+" 用时: "+timeCost+"ms");
	    		timesum+=timeCost;
	    		count++;
	    	}
			
			stmt.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
