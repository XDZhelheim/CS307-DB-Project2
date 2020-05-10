package Project2_12306;

import java.sql.*;
import java.util.*;

public class User {
	static final String driver="org.postgresql.Driver";
    static final String host = "localhost";
    static final String dbname = "project_12306";
    static final String user = "checker";
    static final String password = "201205";
    static final String port = "6666";
    static final String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
    static Connection conn=null;
    static Scanner scan=new Scanner(System.in);
    
    static {
    	try {
			Class.forName(driver);
			conn=DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    enum UserType {ADMIN, PASSENGER}
    private String name;
    private UserType type;
   
    public User(String name, UserType type) {
    	this.name=name;
    	this.type=type;
    }

	public String getName() {
		return name;
	}

	public UserType getType() {
		return type;
	}
	
	public static User login() throws Exception {
    	boolean control=true;
    	String name=null, pw=null;
    	ResultSet rs=null;
    	PreparedStatement stmt=null;
    	while (control) {
	    	System.out.print("用户名：");
			name=scan.next();
			System.out.print("密码：");
			pw=scan.next();
			
			stmt=conn.prepareStatement("select type from users where user_name=? and password=?;");
			stmt.setString(1, name);
			stmt.setString(2, pw);
			
			rs=stmt.executeQuery();
			if (!rs.next()) {
				System.out.println("用户名或密码有误，请重试");
				continue;
			}
			else {
				System.out.println(name+", 欢迎回来！");
				control=false;
			}
    	}
    	String t=rs.getString("type");
    	rs.close();
    	stmt.close();
		if (t.equals("A"))
			return new User(name, UserType.ADMIN);
		return new User(name, UserType.PASSENGER);
    }
	
	public static User signUp() throws Exception {
		boolean control=true;
		String name=null;
		PreparedStatement stmt=null;
		while (control) {
			System.out.println("-----注册-----");
			boolean control2=true;
			while (control2) {
				System.out.print("请输入用户名：");
				name=scan.next();
				stmt=conn.prepareStatement("select 1 from users where user_name='"+name+"';");
				ResultSet rs=stmt.executeQuery();
				if (rs.next()) {
					System.out.println("用户名重复，请重试");
					continue;
				}
				else {
					control2=false;
					rs.close();
				}
			}
			String pw=null;
			boolean control3=true;
			while (control3) {
				System.out.print("请输入密码：");
				pw=scan.next();
				System.out.print("再次输入密码：");
				String temp=scan.next();
				if (!temp.equals(pw)) {
					System.out.println("密码不一致，请重新输入");
					continue;
				}
				else
					control3=false;
			}
			
			stmt=conn.prepareStatement("insert into users (user_name, password, type) values (?, ?, 'P');");
			stmt.setString(1, name);
			stmt.setString(2, pw);
			try {
				stmt.execute();
			}
			catch (Exception e) {
				System.out.println("User already exist, please try again.");
				continue;
			}
			control=false;
			stmt.close();
		}
		System.out.println("欢迎, "+name+"!");
		return new User(name, UserType.PASSENGER);
	}
	
	public static void closeConnection() throws SQLException {
		conn.close();
	}
	
	public static void logout() {
		Main.current_user=null;
		Main.control=-1;
	}
	
	//--------------------------------------------------------------------------------------------------------------
	
	public static ArrayList<TrainQuery> getTrainQueryResult(String start, String arrive) throws SQLException {
		String sql="with temp as (select stop_num, arrive_time, depart_time, price_from_start_station, spear_seat, t.train_id, s.station_id, station_name, train_num, train_type "
				+ "from schedule join station as s on schedule.station_id = s.station_id join train as t on schedule.train_id = t.train_id) " + 
				"select t1.train_num    as tn," + 
				"       t1.train_id     as id," + 
				"       t1.station_name as from," + 
				"       t1.stop_num     as stop1," +
				"       t2.station_name as to," + 
				"       t2.stop_num     as stop2," +
				"       t1.depart_time  as dt," + 
				"       t2.arrive_time  as at," + 
				"       t1.train_type   as ty," + 
				"       t2.price_from_start_station-t1.price_from_start_station as pr " +
				"from (select train_num, train_id, station_name, depart_time, train_type, stop_num, price_from_start_station from temp where station_name like ?) as t1 " + 
				"join (select train_num, station_name, arrive_time, stop_num, price_from_start_station from temp where station_name like ?) as t2 on t1.train_num = t2.train_num and t1.stop_num<t2.stop_num;";
		PreparedStatement stmt=conn.prepareStatement(sql);
		stmt.setString(1, "%"+start+"%");
		stmt.setString(2, "%"+arrive+"%");
		ResultSet rs=stmt.executeQuery();
		String tn=null, from=null, to=null, dt=null, at=null, ty=null;
		int id=-1;
		int stop1=-1, stop2=-1;
		int ti=-1;
		double pr=-1;
		PreparedStatement ticketquery=null;
		ResultSet ticketresult=null;
		ArrayList<TrainQuery> resultlist=new ArrayList<>();
		while (rs.next()) {
			tn=rs.getString("tn");
			id=rs.getInt("id");
			from=rs.getString("from");
			stop1=rs.getInt("stop1");
			to=rs.getString("to");
			stop2=rs.getInt("stop2");
			dt=rs.getString("dt");
			at=rs.getString("at");
			ty=rs.getString("ty");
			pr=rs.getDouble("pr");
			
			if (dt==null)
				dt="        ";
			if (at==null)
				at="        ";
			
			ticketquery=conn.prepareStatement("select min(spear_seat) as ti from schedule where train_id=? and stop_num between ? and ?");
			ticketquery.setInt(1, id);
			ticketquery.setInt(2, stop1);
			ticketquery.setInt(3, stop2-1);
			
			ticketresult=ticketquery.executeQuery();
			while (ticketresult.next())
				ti=ticketresult.getInt("ti");
			
			resultlist.add(new TrainQuery(tn, from, to, dt, at, ty, ti, pr, id, stop1, stop2));
		}
		ticketresult.close();
		ticketquery.close();
		rs.close();
		stmt.close();
		Collections.sort(resultlist, new Comparator<TrainQuery>() {
			@Override
			public int compare(TrainQuery o1, TrainQuery o2) {
				return o1.getTrain_num().compareTo(o2.getTrain_num());
			}
		});
		return resultlist;
	}
	
	public void queryTrain(String start, String arrive) throws SQLException {
		ArrayList<TrainQuery> resultlist=getTrainQueryResult(start, arrive);
		System.out.println("车次查询结果："+start+"→"+arrive);
		for (TrainQuery temp:resultlist)
			System.out.println(temp);
	}
	
	public void reserveTicket(String start, String arrive) throws SQLException {
		ArrayList<TrainQuery> trains=getTrainQueryResult(start, arrive);
		if (trains.isEmpty()) {
			System.out.println("无车次，请检查出发地与到达地！");
			return;
		}
		System.out.println("为您查询到以下车次:");
		for (int i=0;i<trains.size();i++)
			System.out.println((i+1)+". "+trains.get(i));
		System.out.println("选择您要订票的车次编号: ");
		int num=scan.nextInt();
		TrainQuery tq=trains.get(num-1);
		System.out.println("请输入身份证号: ");
		String pid=scan.next();
		
		String sql="insert into orders (user_name, person_id, train_num, start_station, arrive_station, price) values (?, ?, ?, ?, ?, ?);";
		PreparedStatement stmt=conn.prepareStatement(sql);
		stmt.setString(1, this.name);
		stmt.setString(2, pid);
		stmt.setString(3, tq.getTrain_num());
		stmt.setString(4, tq.getDepart_station());
		stmt.setString(5, tq.getArrive_station());
		stmt.setDouble(6, tq.getPrice());
		try {
			stmt.execute();
		} catch (SQLException e) {
			System.out.println("身份证号无效");
			return;
		}
		sql="update schedule set spear_seat=spear_seat-1 where train_id=(select train_id from train where train_num=?) and stop_num between ? and ?;";
		stmt=conn.prepareStatement(sql);
		stmt.setString(1, tq.getTrain_num());
		stmt.setInt(2, tq.getStop1());
		stmt.setInt(3, tq.getStop2()-1);
		stmt.execute();
		System.out.println("订票成功！");
	}
	
}
