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
	
	public ArrayList<TrainQuery> queryTrain(String start, String arrive) throws SQLException {
		String sql="select t1.train_num as id, t1.station_name as from, t1.stop_num as stop1, t2.station_name as to, t2.stop_num as stop2, t1.depart_time as dt, t2.arrive_time as at, t1.train_type as ty"
				+ " from (select * from train where station_name like ?) as t1 join (select * from train where station_name like ?) as t2 "
				+ "on t1.train_num=t2.train_num and t1.stop_num<t2.stop_num;";
		PreparedStatement stmt=conn.prepareStatement(sql);
		stmt.setString(1, "%"+start+"%");
		stmt.setString(2, "%"+arrive+"%");
		ResultSet rs=stmt.executeQuery();
		String id=null, from=null, to=null, dt=null, at=null, ty=null;
		int stop1=-1, stop2=-1;
		int ti=-1;
		PreparedStatement ticketquery=null;
		ResultSet ticketresult=null;
		ArrayList<TrainQuery> resultlist=new ArrayList<>();
		while (rs.next()) {
			id=rs.getString("id");
			from=rs.getString("from");
			stop1=rs.getInt("stop1");
			to=rs.getString("to");
			stop2=rs.getInt("stop2");
			dt=rs.getString("dt");
			at=rs.getString("at");
			ty=rs.getString("ty");
			
			if (dt==null)
				dt="        ";
			if (at==null)
				at="        ";
			
			ticketquery=conn.prepareStatement("select min(spear_seat) as ti from train where train_num=? and stop_num between ? and ?");
			ticketquery.setString(1, id);
			ticketquery.setInt(2, stop1);
			ticketquery.setInt(3, stop2);
			
			ticketresult=ticketquery.executeQuery();
			while (ticketresult.next())
				ti=ticketresult.getInt("ti");
			
			resultlist.add(new TrainQuery(id, from, to, dt, at, ty, ti));
		}
		ticketresult.close();
		ticketquery.close();
		rs.close();
		stmt.close();
		System.out.println("车次查询结果："+start+"→"+arrive);
		for (TrainQuery temp:resultlist)
			System.out.println(temp);
		return resultlist;
	}
	
	public void reserveTicket(String start, String arrive) throws SQLException {
		ArrayList<TrainQuery> trains=queryTrain(start, arrive);
		
	}
	
}
