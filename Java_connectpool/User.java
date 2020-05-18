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
				System.out.println("错误：用户已存在");
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
	
	private static ArrayList<TrainQuery> getTrainQueryResult(String start, String arrive) throws SQLException {
		String sql=
				"select t1.train_num    as tn," + 
				"       t1.station_name as from," + 
				"       t1.stop_num     as stop1," +
				"       t2.station_name as to," + 
				"       t2.stop_num     as stop2," +
				"       t1.depart_time  as dt," + 
				"       t2.arrive_time  as at," + 
				"       t1.train_type   as ty," + 
				"       t2.price_from_start_station-t1.price_from_start_station as pr " +
				"from (select train_num, station_name, depart_time, train_type, stop_num, price_from_start_station from vpath where station_name like ?) as t1 " + 
				"join (select train_num, station_name, arrive_time, stop_num, price_from_start_station from vpath where station_name like ?) as t2 on t1.train_num = t2.train_num and t1.stop_num<t2.stop_num;";
		PreparedStatement stmt=conn.prepareStatement(sql);
		stmt.setString(1, "%"+start+"%");
		stmt.setString(2, "%"+arrive+"%");
		ResultSet rs=stmt.executeQuery();
		String tn=null, from=null, to=null, dt=null, at=null, ty=null;
		int stop1=-1, stop2=-1;
		int ti=-1;
		double pr=-1;
		PreparedStatement ticketquery=null;
		ResultSet ticketresult=null;
		ArrayList<TrainQuery> resultlist=new ArrayList<>();
		while (rs.next()) {
			tn=rs.getString("tn");
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
			
			ticketquery=conn.prepareStatement("select min_seat(?, ?, ?) as ti;");
			ticketquery.setString(1, tn);
			ticketquery.setInt(2, stop1);
			ticketquery.setInt(3, stop2);
			
			ticketresult=ticketquery.executeQuery();
			while (ticketresult.next())
				ti=ticketresult.getInt("ti");
			
			resultlist.add(new TrainQuery(tn, from, to, dt, at, ty, ti, pr, stop1, stop2));
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
	
	public ArrayList<TrainQuery> queryTrain(String start, String arrive) throws SQLException {
		ArrayList<TrainQuery> resultlist=getTrainQueryResult(start, arrive);
		if (resultlist.isEmpty()) {
			System.out.println("无车次，请检查出发地与到达地！");
			return null;
		}
		System.out.println("车次查询结果："+start+"→"+arrive);
		for (int i=0;i<resultlist.size();i++)
			System.out.println((i+1)+". "+resultlist.get(i));
		return resultlist;
	}
	
	public void reserveTicket(String start, String arrive, boolean control, ArrayList<TrainQuery> resultlist) throws SQLException {
		ArrayList<TrainQuery> trains=null;
		if (control) {
			trains=resultlist;
		}
		else {
			trains=getTrainQueryResult(start, arrive);
			if (trains.isEmpty()) {
				System.out.println("无车次，请检查出发地与到达地！");
				return;
			}
			System.out.println("为您查询到以下车次:");
			for (int i=0;i<trains.size();i++)
				System.out.println((i+1)+". "+trains.get(i));
		}
		System.out.print("选择您要订票的车次编号: ");
		int num=scan.nextInt();
		while (num<1 || num>trains.size()) {
			System.out.print("无效编号, 请重新输入: ");
			num=scan.nextInt();
		}
		TrainQuery tq=trains.get(num-1);
		System.out.print("请输入身份证号: ");
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
		stmt.close();
		System.out.println("订票成功！");
	}
	
	private ArrayList<OrderQuery> getOrderQueryResult() throws SQLException {
		ArrayList<OrderQuery> list=new ArrayList<>();
		String sql="select order_id, train_num, start_station, arrive_station, person_id, price from orders where user_name=?";
		PreparedStatement stmt=conn.prepareStatement(sql);
		stmt.setString(1, name);
		int id=0;
		String tn=null, ss=null, as=null, pi=null;
		double pr=0;
		ResultSet rs=stmt.executeQuery();
		while (rs.next()) {
			id=rs.getInt("order_id");
			tn=rs.getString("train_num");
			ss=rs.getString("start_station");
			as=rs.getString("arrive_station");
			pi=rs.getString("person_id");
			pr=rs.getDouble("price");
			
			list.add(new OrderQuery(id, tn, ss, as, pi, pr));
		}
		rs.close();
		stmt.close();
		return list;
	}
	
	public ArrayList<OrderQuery> queryOrder() throws SQLException {
		ArrayList<OrderQuery> orders=getOrderQueryResult();
		if (orders.isEmpty()) {
			System.out.println("无订单");
			return null;
		}
		System.out.println("您的订单:");
		for (int i=0;i<orders.size();i++)
			System.out.println((i+1)+". "+orders.get(i));
		return orders;
	}
	
	public void cancelOrder(boolean control, ArrayList<OrderQuery> resultlist) throws SQLException {
		ArrayList<OrderQuery> orders=null;
		if (control)
			orders=resultlist;
		else {
			orders=getOrderQueryResult();
			if (orders.isEmpty()) {
				System.out.println("无订单");
				return;
			}
			System.out.println("您的订单:");
			for (int i=0;i<orders.size();i++)
				System.out.println((i+1)+". "+orders.get(i));
		}
		System.out.print("请输入要取消的订单编号: ");
		int num=scan.nextInt();
		while (num<1 || num>orders.size()) {
			System.out.print("无效编号, 请重新输入: ");
			num=scan.nextInt();
		}
		OrderQuery oq=orders.get(num-1);
		
		String sql="delete from orders where order_id=?;";
		PreparedStatement stmt=conn.prepareStatement(sql);
		stmt.setInt(1, oq.getOrder_id());
		stmt.execute();
		
		sql="update schedule set spear_seat=spear_seat+1 where train_id=(select train_id from train where train_num=?) "
				+ "and stop_num between (select stop_num from vpath where train_num=? and station_name=?) and (select stop_num from vpath where train_num=? and station_name=?)-1;";
		stmt=conn.prepareStatement(sql);
		stmt.setString(1, oq.getTrain_num());
		stmt.setString(2, oq.getTrain_num());
		stmt.setString(3, oq.getStart_station());
		stmt.setString(4, oq.getTrain_num());
		stmt.setString(5, oq.getArrive_station());
		stmt.execute();
		stmt.close();
		System.out.println("订单已取消！");
	}
	
	public void queryTrainInformation(String train_num) throws SQLException {
		String sql="select stop_num as num, station_name as sn, depart_time as dt, arrive_time as at from vpath where train_num=? order by num";
		PreparedStatement stmt=conn.prepareStatement(sql);
		int n=0;
		String sn=null, dt=null, at=null;
		stmt.setString(1, train_num);
		ResultSet rs=stmt.executeQuery();
		while (rs.next()) {
			n=rs.getInt("num");
			sn=rs.getString("sn");
			dt=rs.getString("dt");
			at=rs.getString("at");
			
			if (dt==null)
				dt="        ";
			if (at==null)
				at="        ";
			
			System.out.println("第"+n+"站: "+sn+"\t到达时间: "+at+"\t出发时间: "+dt);
		}
		rs.close();
		stmt.close();
	}
	
	//------------------------------------------------------------------------------------------------------------------------------------------------
	
	public void addTrain() throws SQLException {
		System.out.println("---添加新列车---");
		System.out.print("请输入列车号: ");
		String tn=scan.next();
		PreparedStatement temp=conn.prepareStatement("select train_id from train where train_num=?");
		ResultSet rs=null;
		temp.setString(1, tn);
		rs=temp.executeQuery();
		while (rs.next()) {
			System.out.println("列车已存在, 请重新输入: ");
			tn=scan.next();
			temp.setString(1, tn);
			rs.close();
			rs=temp.executeQuery();
		}
		rs.close();
		System.out.print("请输入列车类型 (普快/特快/高铁/动车/直达/城际/其它): ");
		String type=scan.next();
		while (!(type.equals("普快") || type.equals("特快") || type.equals("高铁") || type.equals("动车") || type.equals("直达") || type.equals("城际") || type.equals("其它"))) {
			System.out.print("类型错误, 请重新输入: ");
			type=scan.next();
		}
		String sql="insert into train (train_num, train_type) values (?, ?)";
		PreparedStatement stmt=conn.prepareStatement(sql);
		stmt.setString(1, tn);
		stmt.setString(2, type);
		stmt.execute();
		System.out.print("请输入经过站点数: ");
		int n=scan.nextInt();
		while (n<=1) {
			System.out.print("输入无效, 请重新输入: ");
			n=scan.nextInt();
		}
		System.out.println("登记车程: ");
		String st=null, at=null, dt=null;
		sql="insert into schedule (stop_num, arrive_time, depart_time, spear_seat, train_id, station_id, price_from_start_station) values (?, ?, ?, ?, (select train_id from train where train_num=?), (select station_id from station where station_name=?), 233);";
		stmt=conn.prepareStatement(sql);
		temp=conn.prepareStatement("select station_id from station where station_name=?");
		for (int i=1;i<=n;i++) {
			System.out.print("第"+i+"站: ");
			st=scan.next();
			temp.setString(1, st);
			rs=temp.executeQuery();
			while (!rs.next()) {
				System.out.println("站名错误, 请重新输入: ");
				st=scan.next();
				temp.setString(1, st);
				rs.close();
				rs=temp.executeQuery();
			}
			rs.close();
			if (i!=1) {
				System.out.print("到达时间 (时:分): ");
				at=scan.next();
				int index=at.indexOf(":");
				int hour=Integer.parseInt(at.substring(0, index));
				int minute=Integer.parseInt(at.substring(index+1, at.length()));
				while (hour>24 || minute>59) {
					System.out.print("无效时间, 请重新输入: ");
					at=scan.next();
					index=at.indexOf(":");
					hour=Integer.parseInt(at.substring(0, index));
					minute=Integer.parseInt(at.substring(index+1, at.length()));
				}
			}
			if (i!=n) {
				System.out.print("出发时间 (时:分): ");
				dt=scan.next();
				int index=dt.indexOf(":");
				int hour=Integer.parseInt(dt.substring(0, index));
				int minute=Integer.parseInt(dt.substring(index+1, dt.length()));
				while (hour>24 || minute>59) {
					System.out.print("无效时间, 请重新输入: ");
					dt=scan.next();
					index=dt.indexOf(":");
					hour=Integer.parseInt(dt.substring(0, index));
					minute=Integer.parseInt(dt.substring(index+1, dt.length()));
				}
			}
			stmt.setInt(1, i);
			if (i!=1)
				stmt.setString(2, at+":00");
			else
				stmt.setString(2, null);
			if (i!=n)
				stmt.setString(3, dt+":00");
			else
				stmt.setString(3, null);
			stmt.setInt(4, new Random().nextInt(100));
			stmt.setString(5, tn);
			stmt.setString(6, st);
			stmt.execute();
		}
		System.out.println("添加成功!");
		temp.close();
		stmt.close();
	}
	
}
