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
				"       t1.train_type   as ty, " + 
				"       t1.schedule_id   as sid1, " + 
				"       t2.schedule_id   as sid2 " + 
				"from (select train_num, station_name, depart_time, train_type, stop_num, schedule_id from inquire_table where station_name like ?) as t1 " + 
				"join (select train_num, station_name, arrive_time, stop_num, schedule_id from inquire_table where station_name like ?) as t2 on t1.train_num = t2.train_num and t1.stop_num<t2.stop_num;";
		PreparedStatement stmt=conn.prepareStatement(sql);
		stmt.setString(1, "%"+start+"%");
		stmt.setString(2, "%"+arrive+"%");
		ResultSet rs=stmt.executeQuery();
		String tn=null, from=null, to=null, dt=null, at=null, ty=null;
		int stop1=-1, stop2=-1, sid1=-1, sid2=-1, dc=-1;
		ArrayList<TrainQuery> resultlist=new ArrayList<>();
		PreparedStatement selectDateChange=conn.prepareStatement("select date_change(?, ?, ?) as dc;");
		while (rs.next()) {
			tn=rs.getString("tn");
			from=rs.getString("from");
			stop1=rs.getInt("stop1");
			to=rs.getString("to");
			stop2=rs.getInt("stop2");
			dt=rs.getString("dt");
			at=rs.getString("at");
			ty=rs.getString("ty");
			sid1=rs.getInt("sid1");
			sid2=rs.getInt("sid2");
			
			if (dt==null)
				dt="        ";
			if (at==null)
				at="        ";
			
			selectDateChange.setString(1, tn);
			selectDateChange.setInt(2, stop1);
			selectDateChange.setInt(3, stop2);
			ResultSet datechangeresult=selectDateChange.executeQuery();
			while (datechangeresult.next())
				dc=datechangeresult.getInt("dc");
			
			resultlist.add(new TrainQuery(tn, from, to, dt, at, ty, stop1, stop2, sid1, sid2, dc));
		}
		selectDateChange.close();
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
	
	public void queryTrain_reserveTicket(String start, String arrive, boolean reserve) throws SQLException {
		ArrayList<TrainQuery> resultlist=getTrainQueryResult(start, arrive);
		if (resultlist.isEmpty()) {
			System.out.println("无车次，请检查出发地与到达地！");
			return;
		}
		System.out.println("车次查询结果："+start+"→"+arrive);
		for (int i=0;i<resultlist.size();i++)
			System.out.println((i+1)+". "+resultlist.get(i));
		
		PreparedStatement selectDate=conn.prepareStatement("select distinct date from rest_seat");
		ResultSet dateResult=selectDate.executeQuery();
		ArrayList<String> dates=new ArrayList<>();
		while (dateResult.next()) 
			dates.add(dateResult.getString("date"));
		dateResult.close();
		selectDate.close();
		Collections.sort(dates);
		
		boolean ctrl=true;
		while (ctrl) {
			PreparedStatement ticketquery=null;
			ResultSet ticketresult=null;
			System.out.print("请选择车次编号查看详细信息: ");
			int num=scan.nextInt();
			while (num<1 || num>resultlist.size()) {
				System.out.print("无效编号, 请重新输入: ");
				num=scan.nextInt();
			}
			TrainQuery tq=resultlist.get(num-1);
			ticketquery=conn.prepareStatement("select min_seat(cast(? as date), ?, ?, ?, ?) as ti;");
			ticketquery.setString(2, tq.getTrain_num());
			ticketquery.setInt(3, tq.getStop1());
			ticketquery.setInt(4, tq.getStop2());
			
			PreparedStatement selectType=conn.prepareStatement("select type_name as t from seat_type where type_id=?");
			ResultSet typeResult=null;
			String type=null;
			double pr=-1;
			PreparedStatement selectPrice=conn.prepareStatement("select p2.pr-p1.pr as pr from (select price_from_start_station as pr from price where schedule_id=? and seat_type=?) as p1 "
					+ "cross join (select price_from_start_station as pr from price where schedule_id=? and seat_type=?) as p2");
			selectPrice.setInt(1, tq.getSid1());
			selectPrice.setInt(3, tq.getSid2());
			ResultSet priceResult=null;
			ArrayList<Double> seat_price=new ArrayList<>();
			ArrayList<String> seats=new ArrayList<>();
			System.out.println("票价: ");
			int n=-1;
			ResultSet numofseat=conn.prepareStatement("select count(type_id) as cnt from seat_type").executeQuery();
			while (numofseat.next())
				n=numofseat.getInt("cnt");
			for (int i=1;i<=n;i++) {
				selectType.setInt(1, i);
				typeResult=selectType.executeQuery();
				while (typeResult.next())
					type=typeResult.getString("t");
				seats.add(type);
				
				selectPrice.setInt(2, i);
				selectPrice.setInt(4, i);
				priceResult=selectPrice.executeQuery();
				while (priceResult.next())
					pr=priceResult.getDouble("pr");
				
				System.out.print("\t"+type+": "+pr);
				seat_price.add(pr);
			}
			
			int ti=-1;
			System.out.println();
			System.out.println("每日余票: ");
			for (String date:dates) {
				ticketquery.setString(1, date);
				System.out.print(date+": ");
				for (int i=1;i<=n;i++) {
					ticketquery.setInt(5, i);
					ticketresult=ticketquery.executeQuery();
					while (ticketresult.next())
						ti=ticketresult.getInt("ti");
					
					type=seats.get(i-1);
					
					String temp="\t"+type+": "+ti;
					System.out.print(temp);
					if (temp.length()<=8) {
						int x=8-temp.length();
						for (int k=0;k<=x;k++)
							System.out.print(" ");
					}
				}
				System.out.println();
			}
			
			typeResult.close();
			selectType.close();
			ticketresult.close();
			ticketquery.close();
			
			String yn="";
			if (!reserve) {
				System.out.print("是否需要订票? (y/n) ");
				yn=scan.next();
			}
			if ((yn.equalsIgnoreCase("y") || yn.equalsIgnoreCase("yes")) || reserve) {
				// reserve ticket
				System.out.println("请选择日期: ");
				for (int i=0;i<dates.size();i++)
					System.out.println((i+1)+". "+dates.get(i));
				int datenum=scan.nextInt();
				while (datenum<1 || datenum>dates.size()) {
					System.out.print("无效编号, 请重新输入: ");
					datenum=scan.nextInt();
				}
				
				System.out.println("请选择座位类型: ");
				for (int i=0;i<seats.size();i++)
					System.out.println((i+1)+". "+seats.get(i));
				int seatnum=scan.nextInt();
				while (seatnum<1 || seatnum>seats.size()) {
					System.out.print("无效编号, 请重新输入: ");
					seatnum=scan.nextInt();
				}
				
				System.out.print("请输入身份证号: ");
				String pid=scan.next();
				
				String sql="insert into orders (user_name, person_id, train_num, start_station, arrive_station, price, order_date, seat_type_id, depart_time, arrive_time, date_change) "
						+ "values (?, ?, ?, ?, ?, ?, cast(? as date), ?, ?, ?, ?);";
				PreparedStatement stmt=conn.prepareStatement(sql);
				stmt.setString(1, this.name);
				stmt.setString(2, pid);
				stmt.setString(3, tq.getTrain_num());
				stmt.setString(4, tq.getDepart_station());
				stmt.setString(5, tq.getArrive_station());
				stmt.setDouble(6, seat_price.get(seatnum-1));
				stmt.setString(7, dates.get(datenum-1));
				stmt.setInt(8, seatnum);
				stmt.setString(9, tq.getDepart_time());
				stmt.setString(10, tq.getArrive_time());
				stmt.setInt(11, tq.getDateChange());
				try {
					stmt.execute();
				} catch (SQLException e) {
					System.out.println("身份证号无效");
					return;
				}
				sql="select subtract_seat(cast(? as date), ?, ?, ?, ?);";
				stmt=conn.prepareStatement(sql);
				stmt.setString(1, dates.get(datenum-1));
				stmt.setString(2, tq.getTrain_num());
				stmt.setInt(3, tq.getStop1());
				stmt.setInt(4, tq.getStop2());
				stmt.setInt(5, seatnum);
				stmt.execute();
				stmt.close();
				System.out.println("订票成功！");
				queryOrder();
			}
			System.out.print("查看其他列车? (y/n) ");
			String ctr=scan.next();
			ctrl=ctr.equalsIgnoreCase("y") || ctr.equalsIgnoreCase("yes");
		}
	}
	
	private ArrayList<OrderQuery> getOrderQueryResult() throws SQLException {
		ArrayList<String> seats=new ArrayList<>();
		ResultSet seattypes=conn.prepareStatement("select type_name as tn from seat_type;").executeQuery();
		while (seattypes.next())
			seats.add(seattypes.getString("tn"));
		seattypes.close();
		
		ArrayList<OrderQuery> list=new ArrayList<>();
		String sql="select order_id, person_id, train_num, start_station, arrive_station, price, "
				+ "order_date, seat_type_id, depart_time, arrive_time, date_change from orders where user_name=?";
		PreparedStatement stmt=conn.prepareStatement(sql);
		stmt.setString(1, name);
		int id=-1, sti=-1, dc=-1;
		String tn=null, ss=null, as=null, pi=null, date=null, dt=null, at=null;
		double pr=0;
		ResultSet rs=stmt.executeQuery();
		while (rs.next()) {
			id=rs.getInt("order_id");
			tn=rs.getString("train_num");
			ss=rs.getString("start_station");
			as=rs.getString("arrive_station");
			pi=rs.getString("person_id");
			pr=rs.getDouble("price");
			date=rs.getString("order_date");
			sti=rs.getInt("seat_type_id");
			dt=rs.getString("depart_time");
			at=rs.getString("arrive_time");
			dc=rs.getInt("date_change");
			
			list.add(new OrderQuery(id, tn, ss, as, pi, pr, date, seats.get(sti-1), dt, at, dc));
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
		
		sql="select addback_seat(cast(? as date), ?, "
				+ "(select stop_num from inquire_table where train_num=? and station_name=?), (select stop_num from inquire_table where train_num=? and station_name=?), "
				+ "(select type_id from seat_type where type_name=?));";
		stmt=conn.prepareStatement(sql);
		stmt.setString(1, oq.getDate());
		stmt.setString(2, oq.getTrain_num());
		stmt.setString(3, oq.getTrain_num());
		stmt.setString(4, oq.getStart_station());
		stmt.setString(5, oq.getTrain_num());
		stmt.setString(6, oq.getArrive_station());
		stmt.setString(7, oq.getSeatType());
		stmt.execute();
		stmt.close();
		System.out.println("订单已取消！");
		queryOrder();
	}
	
	public void queryTrainInformation(String train_num) throws SQLException {
		String sql="select stop_num as num, station_name as sn, depart_time as dt, arrive_time as at from inquire_table where train_num=? order by num";
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
			System.out.print("列车已存在, 请重新输入: ");
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
		sql="insert into schedule (stop_num, arrive_time, depart_time, train_id, station_id) values (?, ?, ?, (select train_id from train where train_num=?), (select station_id from station where station_name=?));";
		stmt=conn.prepareStatement(sql);
		temp=conn.prepareStatement("select station_id from station where station_name=?;");
		
		PreparedStatement selectnewscheduleid=conn.prepareStatement("select max(schedule_id) as newid from schedule;");
		ResultSet newidresult=null;
		PreparedStatement updatePrice=conn.prepareStatement("insert into price (schedule_id, seat_type, price_from_start_station) values (?, ?, modify_price(?, ?));");
		
		PreparedStatement selectDate=conn.prepareStatement("select distinct date from rest_seat");
		ResultSet dateResult=selectDate.executeQuery();
		ArrayList<String> dates=new ArrayList<>();
		while (dateResult.next()) 
			dates.add(dateResult.getString("date"));
		dateResult.close();
		selectDate.close();
		Collections.sort(dates);
		
		PreparedStatement updaterestseat=conn.prepareStatement("insert into rest_seat (date, price_id, rest_ticket) values (cast(? as date), ?, ?);");
		
		PreparedStatement selectpriceid=conn.prepareStatement("select price_id as pid from price where schedule_id=?;");
		ResultSet priceidresult=null;
		
		ResultSet numofseat=conn.prepareStatement("select count(type_id) as cnt from seat_type").executeQuery();
		int nseats=-1;
		while (numofseat.next())
			nseats=numofseat.getInt("cnt");
		numofseat.close();
		
		for (int i=1;i<=n;i++) {
			System.out.print("第"+i+"站: ");
			st=scan.next();
			temp.setString(1, st);
			rs=temp.executeQuery();
			while (!rs.next()) {
				System.out.print("站名错误, 请重新输入: ");
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
			stmt.setString(4, tn);
			stmt.setString(5, st);
			stmt.execute();
			
			newidresult=selectnewscheduleid.executeQuery();
			int newid=-1;
			while (newidresult.next())
				newid=newidresult.getInt("newid");
			updatePrice.setInt(1, newid);
			updatePrice.setInt(3, newid);
			for (int j=1;j<=nseats;j++) {
				updatePrice.setInt(2, j);
				updatePrice.setInt(4, j);
				updatePrice.execute();
			}
			
			selectpriceid.setInt(1, newid);
			
			for (String date:dates) {
				priceidresult=selectpriceid.executeQuery();
				updaterestseat.setString(1, date);
				while (priceidresult.next()) {
					updaterestseat.setInt(2, priceidresult.getInt("pid"));
					updaterestseat.setInt(3, new Random().nextInt(100));
					updaterestseat.execute();
				}
			}
		}
		
		System.out.println("添加成功!");
		priceidresult.close();
		selectpriceid.close();
		selectnewscheduleid.close();
		newidresult.close();
		temp.close();
		stmt.close();
	}
	
}
