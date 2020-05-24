package Project2_12306;

import java.sql.*;
import java.util.*;

public class User {
    static Connection conn=null;
    static Scanner scan=new Scanner(System.in);
    
    static {
    	try {
    		conn=ProxoolConnectionPool.connector.getConn();
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
			stmt.setInt(2, pw.hashCode());
			
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
			stmt.setInt(2, pw.hashCode());
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
	
	public static ArrayList<String> getDates() throws SQLException {
		PreparedStatement selectDate=conn.prepareStatement("select distinct date from rest_seat");
		ResultSet dateResult=selectDate.executeQuery();
		ArrayList<String> dates=new ArrayList<>();
		while (dateResult.next()) 
			dates.add(dateResult.getString("date"));
		dateResult.close();
		selectDate.close();
		Collections.sort(dates);
		return dates;
	}
	
	public static ArrayList<String> getSeats() throws SQLException {
		ArrayList<String> seats=new ArrayList<>();
		ResultSet seattypes=conn.prepareStatement("select type_name as tn from seat_type;").executeQuery();
		while (seattypes.next())
			seats.add(seattypes.getString("tn"));
		seattypes.close();
		return seats;
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
		
		ArrayList<String> dates=getDates();
		
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
			PreparedStatement selectPrice=conn.prepareStatement("select round(cast(p2.pr-p1.pr as numeric), 2) as pr from (select price_from_start_station as pr from price where schedule_id=? and seat_type=?) as p1 "
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
				
				String sql="insert into orders (user_name, person_id, train_num, start_station, arrive_station, price, order_date, seat_type_id, depart_time, arrive_time, date_change) "
						+ "values (?, ?, ?, ?, ?, ?, cast(? as date), ?, ?, ?, ?);";
				PreparedStatement stmt=conn.prepareStatement(sql);
				
				boolean idcontrol=true;
				while (idcontrol) {
					System.out.print("请输入身份证号: ");
					String pid=scan.next();
					
					conn.prepareStatement("begin;").execute();
					
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
						idcontrol=false;
					} catch (SQLException e) {
						System.out.println("身份证号无效");
						conn.prepareStatement("rollback;").execute();
						idcontrol=true;
					}
				}
				
				sql="select subtract_seat(cast(? as date), ?, ?, ?, ?);";
				stmt=conn.prepareStatement(sql);
				stmt.setString(1, dates.get(datenum-1));
				stmt.setString(2, tq.getTrain_num());
				stmt.setInt(3, tq.getStop1());
				stmt.setInt(4, tq.getStop2());
				stmt.setInt(5, seatnum);
				boolean flag=true;
				try {
					stmt.execute();
				}
				catch (SQLException e) {
					System.out.println("无余票, 订票失败！");
					flag=false;
				}
				stmt.close();
				
				conn.prepareStatement("commit;").execute();
				
				if (flag) {
					System.out.println("订票成功！");
					queryOrder();
				}
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
		
		conn.prepareStatement("begin;").execute();
		
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
		
		conn.prepareStatement("commit;").execute();
		
		System.out.println("订单已取消！");
		queryOrder();
	}
	
	public void queryTrainInformation() throws SQLException {
		System.out.print("请输入要查询的列车号: ");
		String tn=scan.next();
		PreparedStatement temp=conn.prepareStatement("select train_id from train where train_num=?");
		ResultSet rs=null;
		temp.setString(1, tn);
		rs=temp.executeQuery();
		while (!rs.next()) {
			System.out.print("列车不存在, 请重新输入: ");
			tn=scan.next();
			temp.setString(1, tn);
			rs.close();
			rs=temp.executeQuery();
		}
		String sql="select stop_num as num, station_name as sn, depart_time as dt, arrive_time as at from inquire_table where train_num=? order by num";
		PreparedStatement stmt=conn.prepareStatement(sql);
		int n=0;
		String sn=null, dt=null, at=null;
		stmt.setString(1, tn);
		rs=stmt.executeQuery();
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
		
		conn.prepareStatement("begin;").execute();
		
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
		
		ArrayList<String> dates=getDates();
		
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
		
		conn.prepareStatement("commit;").execute();
		
		System.out.println("添加成功!");
		priceidresult.close();
		selectpriceid.close();
		selectnewscheduleid.close();
		newidresult.close();
		temp.close();
		stmt.close();
	}
	
	public void deleteTrain() throws SQLException {
		System.out.println("---删除列车---");
		System.out.print("请输入删除的列车号: ");
		String tn=scan.next();
		PreparedStatement temp=conn.prepareStatement("select train_id from train where train_num=?");
		ResultSet rs=null;
		temp.setString(1, tn);
		rs=temp.executeQuery();
		while (!rs.next()) {
			System.out.print("列车不存在, 请重新输入: ");
			tn=scan.next();
			temp.setString(1, tn);
			rs.close();
			rs=temp.executeQuery();
		}
		rs.close();
		
		conn.prepareStatement("begin;").execute();
		
		String sql="delete from rest_seat where price_id in (select price_id from price where schedule_id in (select schedule_id from inquire_table where train_num=?));";
		PreparedStatement stmt=conn.prepareStatement(sql);
		stmt.setString(1, tn);
		stmt.execute();
		
		sql="delete from price where schedule_id in (select schedule_id from inquire_table where train_num=?);";
		stmt=conn.prepareStatement(sql);
		stmt.setString(1, tn);
		stmt.execute();
		
		sql="delete from schedule where train_id=(select train_id from train where train_num=?)";
		stmt=conn.prepareStatement(sql);
		stmt.setString(1, tn);
		stmt.execute();
		
		sql="delete from inquire_table where train_num=?;";
		stmt=conn.prepareStatement(sql);
		stmt.setString(1, tn);
		stmt.execute();
		
		sql="delete from orders where train_num=?;";
		stmt=conn.prepareStatement(sql);
		stmt.setString(1, tn);
		stmt.execute();
		
		sql="delete from train where train_num=?;";
		stmt=conn.prepareStatement(sql);
		stmt.setString(1, tn);
		stmt.execute();
		
		conn.prepareStatement("commit;").execute();
		
		System.out.println("删除成功!");
		stmt.close();
	}
	
	public void insertSchedule() throws SQLException {
		System.out.println("---插入新站点---");
		System.out.print("请输入改动的列车号: ");
		String tn=scan.next();
		PreparedStatement temp=conn.prepareStatement("select train_id from train where train_num=?");
		ResultSet rs=null;
		temp.setString(1, tn);
		rs=temp.executeQuery();
		while (!rs.next()) {
			System.out.print("列车不存在, 请重新输入: ");
			tn=scan.next();
			temp.setString(1, tn);
			rs.close();
			rs=temp.executeQuery();
		}
		
		String sql="select stop_num as num, station_name as sn, depart_time as dt, arrive_time as at from inquire_table where train_num=? order by num";
		PreparedStatement stmt=conn.prepareStatement(sql);
		int n=0;
		String sn=null, dt=null, at=null;
		stmt.setString(1, tn);
		rs=stmt.executeQuery();
		ArrayList<String> dtimes=new ArrayList<>(), atimes=new ArrayList<>();
		while (rs.next()) {
			n=rs.getInt("num");
			sn=rs.getString("sn");
			dt=rs.getString("dt");
			at=rs.getString("at");
			
			if (dt==null)
				dt="        ";
			if (at==null)
				at="        ";
			
			dtimes.add(dt.substring(0, 5));
			atimes.add(at.substring(0, 5));
			
			System.out.println("第"+n+"站: "+sn+"\t到达时间: "+at+"\t出发时间: "+dt);
		}
		
		System.out.print("请输入插入站点的位置(第x站前): ");
		int pos=scan.nextInt();
		while (pos<=1 || pos>n) {
			System.out.print("无效编号, 请重新输入: ");
			pos=scan.nextInt();
		}
		
		conn.prepareStatement("begin;").execute();
		PreparedStatement updateotherstation=conn.prepareStatement("update schedule set stop_num=stop_num+1 "
				+ "where train_id=(select train_id from train where train_num=?) and stop_num between ? and (select max(stop_num) from inquire_table where train_num=?);");
		updateotherstation.setString(1, tn);
		updateotherstation.setInt(2, pos);
		updateotherstation.setString(3, tn);
		updateotherstation.execute();
		
		String st=null;
		at=null; dt=null;
		sql="insert into schedule (stop_num, arrive_time, depart_time, train_id, station_id) values (?, ?, ?, (select train_id from train where train_num=?), (select station_id from station where station_name=?));";
		stmt=conn.prepareStatement(sql);
		temp=conn.prepareStatement("select station_id from station where station_name=?;");
		
		PreparedStatement selectnewscheduleid=conn.prepareStatement("select max(schedule_id) as newid from schedule;");
		ResultSet newidresult=null;
		PreparedStatement updatePrice=conn.prepareStatement("insert into price (schedule_id, seat_type, price_from_start_station) values (?, ?, modify_price(?, ?));");
		
		ArrayList<String> dates=getDates();
		
		PreparedStatement updaterestseat=conn.prepareStatement("insert into rest_seat (date, price_id, rest_ticket) values (cast(? as date), ?, ?);");
		
		PreparedStatement selectpriceid=conn.prepareStatement("select price_id as pid from price where schedule_id=?;");
		ResultSet priceidresult=null;
		
		ResultSet numofseat=conn.prepareStatement("select count(type_id) as cnt from seat_type").executeQuery();
		int nseats=-1;
		while (numofseat.next())
			nseats=numofseat.getInt("cnt");
		numofseat.close();
		
		System.out.print("请输入站名: ");
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
		System.out.print("到达时间 (时:分): ");
		at=scan.next();
		int index=at.indexOf(":");
		int hour=Integer.parseInt(at.substring(0, index));
		int minute=Integer.parseInt(at.substring(index+1, at.length()));
		while (hour>24 || minute>59 || at.compareTo(dtimes.get(pos-2))<=0) {
			System.out.print("无效时间, 请重新输入: ");
			at=scan.next();
			index=at.indexOf(":");
			hour=Integer.parseInt(at.substring(0, index));
			minute=Integer.parseInt(at.substring(index+1, at.length()));
		}
		System.out.print("出发时间 (时:分): ");
		dt=scan.next();
		index=dt.indexOf(":");
		hour=Integer.parseInt(dt.substring(0, index));
		minute=Integer.parseInt(dt.substring(index+1, dt.length()));
		while (hour>24 || minute>59 || dt.compareTo(atimes.get(pos-1))>=0) {
			System.out.print("无效时间, 请重新输入: ");
			dt=scan.next();
			index=dt.indexOf(":");
			hour=Integer.parseInt(dt.substring(0, index));
			minute=Integer.parseInt(dt.substring(index+1, dt.length()));
		}
		
		stmt.setInt(1, pos);
		stmt.setString(2, at+":00");
		stmt.setString(3, dt+":00");
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
		
		conn.prepareStatement("commit;").execute();
		stmt.close();
		temp.close();
		selectnewscheduleid.close();
		newidresult.close();
		selectpriceid.close();
		priceidresult.close();
		updateotherstation.close();
		updatePrice.close();
		updaterestseat.close();
		System.out.println("列车信息更新完毕!");
	}
	
	public void deleteSchedule() throws SQLException {
		System.out.println("---删除站点---");
		System.out.print("请输入改动的列车号: ");
		String tn=scan.next();
		PreparedStatement temp=conn.prepareStatement("select train_id from train where train_num=?");
		ResultSet rs=null;
		temp.setString(1, tn);
		rs=temp.executeQuery();
		while (!rs.next()) {
			System.out.print("列车不存在, 请重新输入: ");
			tn=scan.next();
			temp.setString(1, tn);
			rs.close();
			rs=temp.executeQuery();
		}
		rs.close();
		
		String sql="select stop_num as num, station_name as sn, depart_time as dt, arrive_time as at from inquire_table where train_num=? order by num";
		PreparedStatement stmt=conn.prepareStatement(sql);
		int n=0;
		String sn=null, dt=null, at=null;
		stmt.setString(1, tn);
		rs=stmt.executeQuery();
		ArrayList<String> stations=new ArrayList<>();
		while (rs.next()) {
			n=rs.getInt("num");
			sn=rs.getString("sn");
			dt=rs.getString("dt");
			at=rs.getString("at");
			
			if (dt==null)
				dt="        ";
			if (at==null)
				at="        ";
			
			stations.add(sn);
			
			System.out.println("第"+n+"站: "+sn+"\t到达时间: "+at+"\t出发时间: "+dt);
		}
		
		System.out.print("请输入删除的站点序号: ");
		int pos=scan.nextInt();
		while (pos<=1 || pos>=n) {
			System.out.print("无效编号, 请重新输入: ");
			pos=scan.nextInt();
		}
		
		conn.prepareStatement("begin;").execute();
		
		sql="delete from rest_seat where price_id in (select price_id from price where schedule_id=(select schedule_id from inquire_table where train_num=? and stop_num=?));";
		stmt=conn.prepareStatement(sql);
		stmt.setString(1, tn);
		stmt.setInt(2, pos);
		stmt.execute();
		
		sql="delete from price where schedule_id=(select schedule_id from inquire_table where train_num=? and stop_num=?);";
		stmt=conn.prepareStatement(sql);
		stmt.setString(1, tn);
		stmt.setInt(2, pos);
		stmt.execute();
		
		sql="delete from schedule where train_id=(select train_id from train where train_num=?) and stop_num=?";
		stmt=conn.prepareStatement(sql);
		stmt.setString(1, tn);
		stmt.setInt(2, pos);
		stmt.execute();
		
		sql="delete from inquire_table where train_num=? and stop_num=?;";
		stmt=conn.prepareStatement(sql);
		stmt.setString(1, tn);
		stmt.setInt(2, pos);
		stmt.execute();
		
		sql="delete from orders where train_num=? and (start_station=? or arrive_station=?);";
		stmt=conn.prepareStatement(sql);
		stmt.setString(1, tn);
		stmt.setString(2, stations.get(pos-1));
		stmt.setString(3, stations.get(pos-1));
		stmt.execute();
		
		PreparedStatement updateotherstation=conn.prepareStatement("update schedule set stop_num=stop_num-1 "
				+ "where train_id=(select train_id from train where train_num=?) and stop_num between ? and (select max(stop_num) from inquire_table where train_num=?);");
		updateotherstation.setString(1, tn);
		updateotherstation.setInt(2, pos+1);
		updateotherstation.setString(3, tn);
		updateotherstation.execute();
		
		conn.prepareStatement("commit;").execute();
		
		System.out.println("删除成功!");
		stmt.close();
	}
	
	//-------------------------------------------------------------------------------------------------------------------------------------------------
	
	private void reserveRecommendTicket(PathQuery pq) throws SQLException {
		ArrayList<String> seats=getSeats();
		
		System.out.println("请选择第一列车座位类型: ");
		for (int i=0;i<seats.size();i++)
			System.out.println((i+1)+". "+seats.get(i));
		int seatnum1=scan.nextInt();
		while (seatnum1<1 || seatnum1>seats.size()) {
			System.out.print("无效编号, 请重新输入: ");
			seatnum1=scan.nextInt();
		}
		
		int seatnum2=-1;
		if (pq.second_from_stop!=0) {
			System.out.println("请选择第二列车座位类型: ");
			for (int i=0;i<seats.size();i++)
				System.out.println((i+1)+". "+seats.get(i));
			seatnum2=scan.nextInt();
			while (seatnum2<1 || seatnum2>seats.size()) {
				System.out.print("无效编号, 请重新输入: ");
				seatnum2=scan.nextInt();
			}
		}
		
		String sql="insert into orders (user_name, person_id, train_num, start_station, arrive_station, price, order_date, seat_type_id, depart_time, arrive_time, date_change) "
				+ "values (?, ?, ?, ?, ?, ?, cast(? as date), ?, ?, ?, ?);";
		PreparedStatement stmt=conn.prepareStatement(sql);
		
		boolean idctrl=true;
		while (idctrl) {
			System.out.print("请输入身份证号: ");
			String pid=scan.next();
			
			conn.prepareStatement("begin;").execute();
			
			stmt.setString(1, this.name);
			stmt.setString(2, pid);
			
			stmt.setString(3, pq.first_train_num);
			stmt.setString(4, pq.first_from_name);
			stmt.setString(5, pq.first_to_name);
			if (seatnum1==1)
				stmt.setDouble(6, pq.first_price1);
			else if (seatnum1==2)
				stmt.setDouble(6, pq.first_price2);
			else if (seatnum1==3)
				stmt.setDouble(6, pq.first_price3);
			else if (seatnum1==4)
				stmt.setDouble(6, pq.first_price4);
			stmt.setString(7, pq.date);
			stmt.setInt(8, seatnum1);
			stmt.setString(9, pq.first_depart);
			stmt.setString(10, pq.first_arrive);
			stmt.setInt(11, pq.datechange1);
			try {
				stmt.execute();
				idctrl=false;
			} catch (SQLException e) {
				System.out.println("身份证号无效");
				conn.prepareStatement("rollback;").execute();
				idctrl=true;
			}
			
			if (pq.second_from_stop!=0) {
				stmt.setString(3, pq.second_train_num);
				stmt.setString(4, pq.first_to_name);
				stmt.setString(5, pq.second_to_name);
				if (seatnum2==1)
					stmt.setDouble(6, pq.second_price1);
				else if (seatnum2==2)
					stmt.setDouble(6, pq.second_price2);
				else if (seatnum2==3)
					stmt.setDouble(6, pq.second_price3);
				else if (seatnum2==4)
					stmt.setDouble(6, pq.second_price4);
				stmt.setString(7, pq.date);
				stmt.setInt(8, seatnum2);
				stmt.setString(9, pq.second_depart);
				stmt.setString(10, pq.second_arrive);
				stmt.setInt(11, pq.datechange2);
				stmt.execute();
			}
		}
		
		sql="select subtract_seat(cast(? as date), ?, ?, ?, ?);";
		stmt=conn.prepareStatement(sql);
		stmt.setString(1, pq.date);
		
		stmt.setString(2, pq.first_train_num);
		stmt.setInt(3, pq.first_from_stop);
		stmt.setInt(4, pq.first_to_stop);
		stmt.setInt(5, seatnum1);
		boolean flag=true;
		try {
			stmt.execute();
		}
		catch (SQLException e) {
			System.out.println("无余票, 订票失败！");
			flag=false;
		}
		
		if (pq.second_from_stop!=0 && flag) {
			stmt.setString(2, pq.second_train_num);
			stmt.setInt(3, pq.second_from_stop);
			stmt.setInt(4, pq.second_to_stop);
			stmt.setInt(5, seatnum2);
			try {
				stmt.execute();
			}
			catch (SQLException e) {
				System.out.println("无余票, 订票失败！");
				flag=false;
			}
		}
		
		stmt.close();
		
		conn.prepareStatement("commit;").execute();
		
		if (flag) {
			System.out.println("订票成功！");
			queryOrder();
		}
	}
	
	public void accurateSearchLowestPrice() throws SQLException {
		System.out.print("请输入出发站: ");
		String start=scan.next();
		System.out.print("请输入到达站: ");
		String arrive=scan.next();
		
		System.out.println("请选择日期: ");
		ArrayList<String> dates=getDates();
		for (int i=0;i<dates.size();i++) 
			System.out.println((i+1)+". "+dates.get(i));
		int datenum=scan.nextInt();
		String date=dates.get(datenum-1);
		
		PreparedStatement aplp=conn.prepareStatement("select * from accurate_path_lowest_price(cast(? as date), ?, ?, ?);");
		ResultSet rs=null;
		aplp.setString(1, date);
		aplp.setString(2, start);
		aplp.setString(3, arrive);
		
		
		String first_train_num=null, first_train_type=null, first_from_name=null, first_to_name=null;
		int first_from_stop=-1, first_to_stop=-1;
		String first_depart=null, first_arrive=null;
		int first_seat1=-1, first_seat2=-1, first_seat3=-1, first_seat4=-1;
		double first_price1=-1, first_price2=-1, first_price3=-1, first_price4=-1;
		
		String second_train_num=null, second_train_type=null, second_to_name=null;
		int second_from_stop=-1, second_to_stop=-1;
		String second_depart=null, second_arrive=null;
		int second_seat1=-1, second_seat2=-1, second_seat3=-1, second_seat4=-1;
		double second_price1=-1, second_price2=-1, second_price3=-1, second_price4=-1;
		
		int datechange1=-1, datechange2=-1;
		
		String total_time=null;
		double total_lowest_price=-1;
		
		int page=1;
		while (page!=0) {
			ArrayList<PathQuery> pathlist=null;
			
			aplp.setInt(4, page);
			pathlist=new ArrayList<>();
			rs=aplp.executeQuery();
			PreparedStatement selectDateChange=conn.prepareStatement("select date_change(?, ?, ?) as dc;");
			ResultSet datechangeresult=null;
			while (rs.next()) {
				first_train_num=rs.getString("first_train_num");
				first_train_type=rs.getString("first_train_type");
				first_from_name=rs.getString("first_from_name");
				first_to_name=rs.getString("first_to_name");
				first_from_stop=rs.getInt("first_from_stop");
				first_to_stop=rs.getInt("first_to_stop");
				first_depart=rs.getString("first_depart");
				first_arrive=rs.getString("first_arrive");
				first_seat1=rs.getInt("first_seat1");
				first_seat2=rs.getInt("first_seat2");
				first_seat3=rs.getInt("first_seat3");
				first_seat4=rs.getInt("first_seat4");
				first_price1=rs.getDouble("first_price1");
				first_price2=rs.getDouble("first_price2");
				first_price3=rs.getDouble("first_price3");
				first_price4=rs.getDouble("first_price4");
				
				selectDateChange.setString(1, first_train_num);
				selectDateChange.setInt(2, first_from_stop);
				selectDateChange.setInt(3, first_to_stop);
				datechangeresult=selectDateChange.executeQuery();
				while (datechangeresult.next())
					datechange1=datechangeresult.getInt("dc");
				
				second_train_num=rs.getString("second_train_num");
				second_train_type=rs.getString("second_train_type");
				second_to_name=rs.getString("second_to_name");
				second_from_stop=rs.getInt("second_from_stop");
				second_to_stop=rs.getInt("second_to_stop");
				second_depart=rs.getString("second_leave");
				second_arrive=rs.getString("second_arrive");
				second_seat1=rs.getInt("second_seat1");
				second_seat2=rs.getInt("second_seat2");
				second_seat3=rs.getInt("second_seat3");
				second_seat4=rs.getInt("second_seat4");
				second_price1=rs.getDouble("second_price1");
				second_price2=rs.getDouble("second_price2");
				second_price3=rs.getDouble("second_price3");
				second_price4=rs.getDouble("second_price4");
				
				selectDateChange.setString(1, second_train_num);
				selectDateChange.setInt(2, second_from_stop);
				selectDateChange.setInt(3, second_to_stop);
				datechangeresult=selectDateChange.executeQuery();
				while (datechangeresult.next())
					datechange2=datechangeresult.getInt("dc");
				
				total_time=rs.getString("total_time");
				total_lowest_price=rs.getDouble("total_lowest_price");
				
				pathlist.add(new PathQuery(date, datechange1, datechange2, first_train_num, first_train_type, first_from_name, first_to_name, first_from_stop, first_to_stop, first_depart, first_arrive, first_seat1, first_seat2, first_seat3, first_seat4, first_price1, first_price2, first_price3, first_price4, 
						second_train_num, second_train_type, second_to_name, second_from_stop, second_to_stop, second_depart, second_arrive, second_seat1, second_seat2, second_seat3, second_seat4, second_price1, second_price2, second_price3, second_price4, total_time, total_lowest_price));
			}
			
			if (pathlist.isEmpty())
				System.out.println("本页无信息");
			else {
				System.out.println("(第"+page+"页)");
				System.out.println("最低票价, 为您查询到以下线路: ");
				for (int i=0;i<pathlist.size();i++) {
					System.out.println("路线"+(i+1)+":");
					System.out.println(pathlist.get(i));
					System.out.println();
				}
				
				boolean ctrl=true;
				while (ctrl) {
					System.out.print("请选择路线编号查看详细信息: ");
					int num=scan.nextInt();
					while (num<1 || num>pathlist.size()) {
						System.out.print("无效编号, 请重新输入: ");
						num=scan.nextInt();
					}
					PathQuery pq=pathlist.get(num-1);
					pq.showDetail();
					
					System.out.print("是否需要订票? (y/n) ");
					String yn=scan.next();
					if (yn.equalsIgnoreCase("y") || yn.equalsIgnoreCase("yes"))
						reserveRecommendTicket(pq);
					
					System.out.print("查看其他车次? (y/n) ");
					String ctr=scan.next();
					ctrl=ctr.equalsIgnoreCase("y") || ctr.equalsIgnoreCase("yes");
				}
			}
			System.out.print("请输入跳转的页数 (0表示退出): ");
			page=scan.nextInt();
		}
	}
	
	public void accurateSearchMinTransfer() throws SQLException {
		System.out.print("请输入出发站: ");
		String start=scan.next();
		System.out.print("请输入到达站: ");
		String arrive=scan.next();
		
		System.out.println("请选择日期: ");
		ArrayList<String> dates=getDates();
		for (int i=0;i<dates.size();i++) 
			System.out.println((i+1)+". "+dates.get(i));
		int datenum=scan.nextInt();
		String date=dates.get(datenum-1);
		
		PreparedStatement aplp=conn.prepareStatement("select * from accurate_path_minimum_transfer(cast(? as date), ?, ?, ?);");
		ResultSet rs=null;
		aplp.setString(1, date);
		aplp.setString(2, start);
		aplp.setString(3, arrive);
		
		
		String first_train_num=null, first_train_type=null, first_from_name=null, first_to_name=null;
		int first_from_stop=-1, first_to_stop=-1;
		String first_depart=null, first_arrive=null;
		int first_seat1=-1, first_seat2=-1, first_seat3=-1, first_seat4=-1;
		double first_price1=-1, first_price2=-1, first_price3=-1, first_price4=-1;
		
		String second_train_num=null, second_train_type=null, second_to_name=null;
		int second_from_stop=-1, second_to_stop=-1;
		String second_depart=null, second_arrive=null;
		int second_seat1=-1, second_seat2=-1, second_seat3=-1, second_seat4=-1;
		double second_price1=-1, second_price2=-1, second_price3=-1, second_price4=-1;
		
		int datechange1=-1, datechange2=-1;
		
		String total_time=null;
		double total_lowest_price=-1;
		
		int page=1;
		while (page!=0) {
			ArrayList<PathQuery> pathlist=null;
			
			aplp.setInt(4, page);
			pathlist=new ArrayList<>();
			rs=aplp.executeQuery();
			PreparedStatement selectDateChange=conn.prepareStatement("select date_change(?, ?, ?) as dc;");
			ResultSet datechangeresult=null;
			while (rs.next()) {
				first_train_num=rs.getString("first_train_num");
				first_train_type=rs.getString("first_train_type");
				first_from_name=rs.getString("first_from_name");
				first_to_name=rs.getString("first_to_name");
				first_from_stop=rs.getInt("first_from_stop");
				first_to_stop=rs.getInt("first_to_stop");
				first_depart=rs.getString("first_depart");
				first_arrive=rs.getString("first_arrive");
				first_seat1=rs.getInt("first_seat1");
				first_seat2=rs.getInt("first_seat2");
				first_seat3=rs.getInt("first_seat3");
				first_seat4=rs.getInt("first_seat4");
				first_price1=rs.getDouble("first_price1");
				first_price2=rs.getDouble("first_price2");
				first_price3=rs.getDouble("first_price3");
				first_price4=rs.getDouble("first_price4");
				
				selectDateChange.setString(1, first_train_num);
				selectDateChange.setInt(2, first_from_stop);
				selectDateChange.setInt(3, first_to_stop);
				datechangeresult=selectDateChange.executeQuery();
				while (datechangeresult.next())
					datechange1=datechangeresult.getInt("dc");
				
				second_train_num=rs.getString("second_train_num");
				second_train_type=rs.getString("second_train_type");
				second_to_name=rs.getString("second_to_name");
				second_from_stop=rs.getInt("second_from_stop");
				second_to_stop=rs.getInt("second_to_stop");
				second_depart=rs.getString("second_leave");
				second_arrive=rs.getString("second_arrive");
				second_seat1=rs.getInt("second_seat1");
				second_seat2=rs.getInt("second_seat2");
				second_seat3=rs.getInt("second_seat3");
				second_seat4=rs.getInt("second_seat4");
				second_price1=rs.getDouble("second_price1");
				second_price2=rs.getDouble("second_price2");
				second_price3=rs.getDouble("second_price3");
				second_price4=rs.getDouble("second_price4");
				
				selectDateChange.setString(1, second_train_num);
				selectDateChange.setInt(2, second_from_stop);
				selectDateChange.setInt(3, second_to_stop);
				datechangeresult=selectDateChange.executeQuery();
				while (datechangeresult.next())
					datechange2=datechangeresult.getInt("dc");
				
				total_time=rs.getString("total_time");
				total_lowest_price=rs.getDouble("total_lowest_price");
				
				pathlist.add(new PathQuery(date, datechange1, datechange2, first_train_num, first_train_type, first_from_name, first_to_name, first_from_stop, first_to_stop, first_depart, first_arrive, first_seat1, first_seat2, first_seat3, first_seat4, first_price1, first_price2, first_price3, first_price4, 
						second_train_num, second_train_type, second_to_name, second_from_stop, second_to_stop, second_depart, second_arrive, second_seat1, second_seat2, second_seat3, second_seat4, second_price1, second_price2, second_price3, second_price4, total_time, total_lowest_price));
			}
			
			if (pathlist.isEmpty())
				System.out.println("本页无信息");
			else {
				System.out.println("(第"+page+"页)");
				System.out.println("最少换乘, 为您查询到以下线路: ");
				for (int i=0;i<pathlist.size();i++) {
					System.out.println("路线"+(i+1)+":");
					System.out.println(pathlist.get(i));
					System.out.println();
				}
				
				boolean ctrl=true;
				while (ctrl) {
					System.out.print("请选择路线编号查看详细信息: ");
					int num=scan.nextInt();
					while (num<1 || num>pathlist.size()) {
						System.out.print("无效编号, 请重新输入: ");
						num=scan.nextInt();
					}
					PathQuery pq=pathlist.get(num-1);
					pq.showDetail();
					
					System.out.print("是否需要订票? (y/n) ");
					String yn=scan.next();
					if (yn.equalsIgnoreCase("y") || yn.equalsIgnoreCase("yes"))
						reserveRecommendTicket(pq);
					
					System.out.print("查看其他车次? (y/n) ");
					String ctr=scan.next();
					ctrl=ctr.equalsIgnoreCase("y") || ctr.equalsIgnoreCase("yes");
				}
			}
			System.out.print("请输入跳转的页数 (0表示退出): ");
			page=scan.nextInt();
		}
	}
	
	public void accurateSearchShortestTime() throws SQLException {
		System.out.print("请输入出发站: ");
		String start=scan.next();
		System.out.print("请输入到达站: ");
		String arrive=scan.next();
		
		System.out.println("请选择日期: ");
		ArrayList<String> dates=getDates();
		for (int i=0;i<dates.size();i++) 
			System.out.println((i+1)+". "+dates.get(i));
		int datenum=scan.nextInt();
		String date=dates.get(datenum-1);
		
		PreparedStatement aplp=conn.prepareStatement("select * from accurate_path_shortest_time(cast(? as date), ?, ?, ?);");
		ResultSet rs=null;
		aplp.setString(1, date);
		aplp.setString(2, start);
		aplp.setString(3, arrive);
		
		
		String first_train_num=null, first_train_type=null, first_from_name=null, first_to_name=null;
		int first_from_stop=-1, first_to_stop=-1;
		String first_depart=null, first_arrive=null;
		int first_seat1=-1, first_seat2=-1, first_seat3=-1, first_seat4=-1;
		double first_price1=-1, first_price2=-1, first_price3=-1, first_price4=-1;
		
		String second_train_num=null, second_train_type=null, second_to_name=null;
		int second_from_stop=-1, second_to_stop=-1;
		String second_depart=null, second_arrive=null;
		int second_seat1=-1, second_seat2=-1, second_seat3=-1, second_seat4=-1;
		double second_price1=-1, second_price2=-1, second_price3=-1, second_price4=-1;
		
		int datechange1=-1, datechange2=-1;
		
		String total_time=null;
		double total_lowest_price=-1;
		
		int page=1;
		while (page!=0) {
			ArrayList<PathQuery> pathlist=null;
			
			aplp.setInt(4, page);
			pathlist=new ArrayList<>();
			rs=aplp.executeQuery();
			PreparedStatement selectDateChange=conn.prepareStatement("select date_change(?, ?, ?) as dc;");
			ResultSet datechangeresult=null;
			while (rs.next()) {
				first_train_num=rs.getString("first_train_num");
				first_train_type=rs.getString("first_train_type");
				first_from_name=rs.getString("first_from_name");
				first_to_name=rs.getString("first_to_name");
				first_from_stop=rs.getInt("first_from_stop");
				first_to_stop=rs.getInt("first_to_stop");
				first_depart=rs.getString("first_depart");
				first_arrive=rs.getString("first_arrive");
				first_seat1=rs.getInt("first_seat1");
				first_seat2=rs.getInt("first_seat2");
				first_seat3=rs.getInt("first_seat3");
				first_seat4=rs.getInt("first_seat4");
				first_price1=rs.getDouble("first_price1");
				first_price2=rs.getDouble("first_price2");
				first_price3=rs.getDouble("first_price3");
				first_price4=rs.getDouble("first_price4");
				
				selectDateChange.setString(1, first_train_num);
				selectDateChange.setInt(2, first_from_stop);
				selectDateChange.setInt(3, first_to_stop);
				datechangeresult=selectDateChange.executeQuery();
				while (datechangeresult.next())
					datechange1=datechangeresult.getInt("dc");
				
				second_train_num=rs.getString("second_train_num");
				second_train_type=rs.getString("second_train_type");
				second_to_name=rs.getString("second_to_name");
				second_from_stop=rs.getInt("second_from_stop");
				second_to_stop=rs.getInt("second_to_stop");
				second_depart=rs.getString("second_leave");
				second_arrive=rs.getString("second_arrive");
				second_seat1=rs.getInt("second_seat1");
				second_seat2=rs.getInt("second_seat2");
				second_seat3=rs.getInt("second_seat3");
				second_seat4=rs.getInt("second_seat4");
				second_price1=rs.getDouble("second_price1");
				second_price2=rs.getDouble("second_price2");
				second_price3=rs.getDouble("second_price3");
				second_price4=rs.getDouble("second_price4");
				
				selectDateChange.setString(1, second_train_num);
				selectDateChange.setInt(2, second_from_stop);
				selectDateChange.setInt(3, second_to_stop);
				datechangeresult=selectDateChange.executeQuery();
				while (datechangeresult.next())
					datechange2=datechangeresult.getInt("dc");
				
				total_time=rs.getString("total_time");
				total_lowest_price=rs.getDouble("total_lowest_price");
				
				pathlist.add(new PathQuery(date, datechange1, datechange2, first_train_num, first_train_type, first_from_name, first_to_name, first_from_stop, first_to_stop, first_depart, first_arrive, first_seat1, first_seat2, first_seat3, first_seat4, first_price1, first_price2, first_price3, first_price4, 
						second_train_num, second_train_type, second_to_name, second_from_stop, second_to_stop, second_depart, second_arrive, second_seat1, second_seat2, second_seat3, second_seat4, second_price1, second_price2, second_price3, second_price4, total_time, total_lowest_price));
			}
			
			if (pathlist.isEmpty())
				System.out.println("本页无信息");
			else {
				System.out.println("(第"+page+"页)");
				System.out.println("最短用时, 为您查询到以下线路: ");
				for (int i=0;i<pathlist.size();i++) {
					System.out.println("路线"+(i+1)+":");
					System.out.println(pathlist.get(i));
					System.out.println();
				}
				
				boolean ctrl=true;
				while (ctrl) {
					System.out.print("请选择路线编号查看详细信息: ");
					int num=scan.nextInt();
					while (num<1 || num>pathlist.size()) {
						System.out.print("无效编号, 请重新输入: ");
						num=scan.nextInt();
					}
					PathQuery pq=pathlist.get(num-1);
					pq.showDetail();
					
					System.out.print("是否需要订票? (y/n) ");
					String yn=scan.next();
					if (yn.equalsIgnoreCase("y") || yn.equalsIgnoreCase("yes"))
						reserveRecommendTicket(pq);
					
					System.out.print("查看其他车次? (y/n) ");
					String ctr=scan.next();
					ctrl=ctr.equalsIgnoreCase("y") || ctr.equalsIgnoreCase("yes");
				}
			}
			System.out.print("请输入跳转的页数 (0表示退出): ");
			page=scan.nextInt();
		}
	}
	
	public void accurateSearchRecommend() throws SQLException {
		System.out.print("请输入出发站: ");
		String start=scan.next();
		System.out.print("请输入到达站: ");
		String arrive=scan.next();
		
		System.out.println("请选择日期: ");
		ArrayList<String> dates=getDates();
		for (int i=0;i<dates.size();i++) 
			System.out.println((i+1)+". "+dates.get(i));
		int datenum=scan.nextInt();
		String date=dates.get(datenum-1);
		
		PreparedStatement aplp=conn.prepareStatement("select * from accurate_path_recommend(cast(? as date), ?, ?, ?);");
		ResultSet rs=null;
		aplp.setString(1, date);
		aplp.setString(2, start);
		aplp.setString(3, arrive);
		
		
		String first_train_num=null, first_train_type=null, first_from_name=null, first_to_name=null;
		int first_from_stop=-1, first_to_stop=-1;
		String first_depart=null, first_arrive=null;
		int first_seat1=-1, first_seat2=-1, first_seat3=-1, first_seat4=-1;
		double first_price1=-1, first_price2=-1, first_price3=-1, first_price4=-1;
		
		String second_train_num=null, second_train_type=null, second_to_name=null;
		int second_from_stop=-1, second_to_stop=-1;
		String second_depart=null, second_arrive=null;
		int second_seat1=-1, second_seat2=-1, second_seat3=-1, second_seat4=-1;
		double second_price1=-1, second_price2=-1, second_price3=-1, second_price4=-1;
		
		int datechange1=-1, datechange2=-1;
		
		String total_time=null;
		double total_lowest_price=-1;
		
		int page=1;
		while (page!=0) {
			ArrayList<PathQuery> pathlist=null;
			
			aplp.setInt(4, page);
			pathlist=new ArrayList<>();
			rs=aplp.executeQuery();
			PreparedStatement selectDateChange=conn.prepareStatement("select date_change(?, ?, ?) as dc;");
			ResultSet datechangeresult=null;
			while (rs.next()) {
				first_train_num=rs.getString("first_train_num");
				first_train_type=rs.getString("first_train_type");
				first_from_name=rs.getString("first_from_name");
				first_to_name=rs.getString("first_to_name");
				first_from_stop=rs.getInt("first_from_stop");
				first_to_stop=rs.getInt("first_to_stop");
				first_depart=rs.getString("first_depart");
				first_arrive=rs.getString("first_arrive");
				first_seat1=rs.getInt("first_seat1");
				first_seat2=rs.getInt("first_seat2");
				first_seat3=rs.getInt("first_seat3");
				first_seat4=rs.getInt("first_seat4");
				first_price1=rs.getDouble("first_price1");
				first_price2=rs.getDouble("first_price2");
				first_price3=rs.getDouble("first_price3");
				first_price4=rs.getDouble("first_price4");
				
				selectDateChange.setString(1, first_train_num);
				selectDateChange.setInt(2, first_from_stop);
				selectDateChange.setInt(3, first_to_stop);
				datechangeresult=selectDateChange.executeQuery();
				while (datechangeresult.next())
					datechange1=datechangeresult.getInt("dc");
				
				second_train_num=rs.getString("second_train_num");
				second_train_type=rs.getString("second_train_type");
				second_to_name=rs.getString("second_to_name");
				second_from_stop=rs.getInt("second_from_stop");
				second_to_stop=rs.getInt("second_to_stop");
				second_depart=rs.getString("second_leave");
				second_arrive=rs.getString("second_arrive");
				second_seat1=rs.getInt("second_seat1");
				second_seat2=rs.getInt("second_seat2");
				second_seat3=rs.getInt("second_seat3");
				second_seat4=rs.getInt("second_seat4");
				second_price1=rs.getDouble("second_price1");
				second_price2=rs.getDouble("second_price2");
				second_price3=rs.getDouble("second_price3");
				second_price4=rs.getDouble("second_price4");
				
				selectDateChange.setString(1, second_train_num);
				selectDateChange.setInt(2, second_from_stop);
				selectDateChange.setInt(3, second_to_stop);
				datechangeresult=selectDateChange.executeQuery();
				while (datechangeresult.next())
					datechange2=datechangeresult.getInt("dc");
				
				total_time=rs.getString("total_time");
				total_lowest_price=rs.getDouble("total_lowest_price");
				
				pathlist.add(new PathQuery(date, datechange1, datechange2, first_train_num, first_train_type, first_from_name, first_to_name, first_from_stop, first_to_stop, first_depart, first_arrive, first_seat1, first_seat2, first_seat3, first_seat4, first_price1, first_price2, first_price3, first_price4, 
						second_train_num, second_train_type, second_to_name, second_from_stop, second_to_stop, second_depart, second_arrive, second_seat1, second_seat2, second_seat3, second_seat4, second_price1, second_price2, second_price3, second_price4, total_time, total_lowest_price));
			}
			
			if (pathlist.isEmpty())
				System.out.println("本页无信息");
			else {
				System.out.println("(第"+page+"页)");
				System.out.println("系统推荐路线, 为您查询到以下线路: ");
				for (int i=0;i<pathlist.size();i++) {
					System.out.println("路线"+(i+1)+":");
					System.out.println(pathlist.get(i));
					System.out.println();
				}
				
				boolean ctrl=true;
				while (ctrl) {
					System.out.print("请选择路线编号查看详细信息: ");
					int num=scan.nextInt();
					while (num<1 || num>pathlist.size()) {
						System.out.print("无效编号, 请重新输入: ");
						num=scan.nextInt();
					}
					PathQuery pq=pathlist.get(num-1);
					pq.showDetail();
					
					System.out.print("是否需要订票? (y/n) ");
					String yn=scan.next();
					if (yn.equalsIgnoreCase("y") || yn.equalsIgnoreCase("yes"))
						reserveRecommendTicket(pq);
					
					System.out.print("查看其他车次? (y/n) ");
					String ctr=scan.next();
					ctrl=ctr.equalsIgnoreCase("y") || ctr.equalsIgnoreCase("yes");
				}
			}
			System.out.print("请输入跳转的页数 (0表示退出): ");
			page=scan.nextInt();
		}
	}
	
	public void fuzzySearchLowestPrice() throws SQLException {
		System.out.print("请输入出发站: ");
		String start=scan.next();
		System.out.print("请输入到达站: ");
		String arrive=scan.next();
		
		System.out.println("请选择日期: ");
		ArrayList<String> dates=getDates();
		for (int i=0;i<dates.size();i++) 
			System.out.println((i+1)+". "+dates.get(i));
		int datenum=scan.nextInt();
		String date=dates.get(datenum-1);
		
		PreparedStatement aplp=conn.prepareStatement("select * from fuzzy_path_lowest_price(cast(? as date), ?, ?, ?);");
		ResultSet rs=null;
		aplp.setString(1, date);
		aplp.setString(2, start);
		aplp.setString(3, arrive);
		
		
		String first_train_num=null, first_train_type=null, first_from_name=null, first_to_name=null;
		int first_from_stop=-1, first_to_stop=-1;
		String first_depart=null, first_arrive=null;
		int first_seat1=-1, first_seat2=-1, first_seat3=-1, first_seat4=-1;
		double first_price1=-1, first_price2=-1, first_price3=-1, first_price4=-1;
		
		String second_train_num=null, second_train_type=null, second_to_name=null;
		int second_from_stop=-1, second_to_stop=-1;
		String second_depart=null, second_arrive=null;
		int second_seat1=-1, second_seat2=-1, second_seat3=-1, second_seat4=-1;
		double second_price1=-1, second_price2=-1, second_price3=-1, second_price4=-1;
		
		int datechange1=-1, datechange2=-1;
		
		String total_time=null;
		double total_lowest_price=-1;
		
		int page=1;
		while (page!=0) {
			ArrayList<PathQuery> pathlist=null;
			
			aplp.setInt(4, page);
			pathlist=new ArrayList<>();
			rs=aplp.executeQuery();
			PreparedStatement selectDateChange=conn.prepareStatement("select date_change(?, ?, ?) as dc;");
			ResultSet datechangeresult=null;
			while (rs.next()) {
				first_train_num=rs.getString("first_train_num");
				first_train_type=rs.getString("first_train_type");
				first_from_name=rs.getString("first_from_name");
				first_to_name=rs.getString("first_to_name");
				first_from_stop=rs.getInt("first_from_stop");
				first_to_stop=rs.getInt("first_to_stop");
				first_depart=rs.getString("first_depart");
				first_arrive=rs.getString("first_arrive");
				first_seat1=rs.getInt("first_seat1");
				first_seat2=rs.getInt("first_seat2");
				first_seat3=rs.getInt("first_seat3");
				first_seat4=rs.getInt("first_seat4");
				first_price1=rs.getDouble("first_price1");
				first_price2=rs.getDouble("first_price2");
				first_price3=rs.getDouble("first_price3");
				first_price4=rs.getDouble("first_price4");
				
				selectDateChange.setString(1, first_train_num);
				selectDateChange.setInt(2, first_from_stop);
				selectDateChange.setInt(3, first_to_stop);
				datechangeresult=selectDateChange.executeQuery();
				while (datechangeresult.next())
					datechange1=datechangeresult.getInt("dc");
				
				second_train_num=rs.getString("second_train_num");
				second_train_type=rs.getString("second_train_type");
				second_to_name=rs.getString("second_to_name");
				second_from_stop=rs.getInt("second_from_stop");
				second_to_stop=rs.getInt("second_to_stop");
				second_depart=rs.getString("second_leave");
				second_arrive=rs.getString("second_arrive");
				second_seat1=rs.getInt("second_seat1");
				second_seat2=rs.getInt("second_seat2");
				second_seat3=rs.getInt("second_seat3");
				second_seat4=rs.getInt("second_seat4");
				second_price1=rs.getDouble("second_price1");
				second_price2=rs.getDouble("second_price2");
				second_price3=rs.getDouble("second_price3");
				second_price4=rs.getDouble("second_price4");
				
				selectDateChange.setString(1, second_train_num);
				selectDateChange.setInt(2, second_from_stop);
				selectDateChange.setInt(3, second_to_stop);
				datechangeresult=selectDateChange.executeQuery();
				while (datechangeresult.next())
					datechange2=datechangeresult.getInt("dc");
				
				total_time=rs.getString("total_time");
				total_lowest_price=rs.getDouble("total_lowest_price");
				
				pathlist.add(new PathQuery(date, datechange1, datechange2, first_train_num, first_train_type, first_from_name, first_to_name, first_from_stop, first_to_stop, first_depart, first_arrive, first_seat1, first_seat2, first_seat3, first_seat4, first_price1, first_price2, first_price3, first_price4, 
						second_train_num, second_train_type, second_to_name, second_from_stop, second_to_stop, second_depart, second_arrive, second_seat1, second_seat2, second_seat3, second_seat4, second_price1, second_price2, second_price3, second_price4, total_time, total_lowest_price));
			}
			
			if (pathlist.isEmpty())
				System.out.println("本页无信息");
			else {
				System.out.println("(第"+page+"页)");
				System.out.println("最低票价, 为您查询到以下线路: ");
				for (int i=0;i<pathlist.size();i++) {
					System.out.println("路线"+(i+1)+":");
					System.out.println(pathlist.get(i));
					System.out.println();
				}
				
				boolean ctrl=true;
				while (ctrl) {
					System.out.print("请选择路线编号查看详细信息: ");
					int num=scan.nextInt();
					while (num<1 || num>pathlist.size()) {
						System.out.print("无效编号, 请重新输入: ");
						num=scan.nextInt();
					}
					PathQuery pq=pathlist.get(num-1);
					pq.showDetail();
					
					System.out.print("是否需要订票? (y/n) ");
					String yn=scan.next();
					if (yn.equalsIgnoreCase("y") || yn.equalsIgnoreCase("yes"))
						reserveRecommendTicket(pq);
					
					System.out.print("查看其他车次? (y/n) ");
					String ctr=scan.next();
					ctrl=ctr.equalsIgnoreCase("y") || ctr.equalsIgnoreCase("yes");
				}
			}
			System.out.print("请输入跳转的页数 (0表示退出): ");
			page=scan.nextInt();
		}
	}
	
	public void fuzzySearchMinTransfer() throws SQLException {
		System.out.print("请输入出发站: ");
		String start=scan.next();
		System.out.print("请输入到达站: ");
		String arrive=scan.next();
		
		System.out.println("请选择日期: ");
		ArrayList<String> dates=getDates();
		for (int i=0;i<dates.size();i++) 
			System.out.println((i+1)+". "+dates.get(i));
		int datenum=scan.nextInt();
		String date=dates.get(datenum-1);
		
		PreparedStatement aplp=conn.prepareStatement("select * from fuzzy_path_minimum_transfer(cast(? as date), ?, ?, ?);");
		ResultSet rs=null;
		aplp.setString(1, date);
		aplp.setString(2, start);
		aplp.setString(3, arrive);
		
		
		String first_train_num=null, first_train_type=null, first_from_name=null, first_to_name=null;
		int first_from_stop=-1, first_to_stop=-1;
		String first_depart=null, first_arrive=null;
		int first_seat1=-1, first_seat2=-1, first_seat3=-1, first_seat4=-1;
		double first_price1=-1, first_price2=-1, first_price3=-1, first_price4=-1;
		
		String second_train_num=null, second_train_type=null, second_to_name=null;
		int second_from_stop=-1, second_to_stop=-1;
		String second_depart=null, second_arrive=null;
		int second_seat1=-1, second_seat2=-1, second_seat3=-1, second_seat4=-1;
		double second_price1=-1, second_price2=-1, second_price3=-1, second_price4=-1;
		
		int datechange1=-1, datechange2=-1;
		
		String total_time=null;
		double total_lowest_price=-1;
		
		int page=1;
		while (page!=0) {
			ArrayList<PathQuery> pathlist=null;
			
			aplp.setInt(4, page);
			pathlist=new ArrayList<>();
			rs=aplp.executeQuery();
			PreparedStatement selectDateChange=conn.prepareStatement("select date_change(?, ?, ?) as dc;");
			ResultSet datechangeresult=null;
			while (rs.next()) {
				first_train_num=rs.getString("first_train_num");
				first_train_type=rs.getString("first_train_type");
				first_from_name=rs.getString("first_from_name");
				first_to_name=rs.getString("first_to_name");
				first_from_stop=rs.getInt("first_from_stop");
				first_to_stop=rs.getInt("first_to_stop");
				first_depart=rs.getString("first_depart");
				first_arrive=rs.getString("first_arrive");
				first_seat1=rs.getInt("first_seat1");
				first_seat2=rs.getInt("first_seat2");
				first_seat3=rs.getInt("first_seat3");
				first_seat4=rs.getInt("first_seat4");
				first_price1=rs.getDouble("first_price1");
				first_price2=rs.getDouble("first_price2");
				first_price3=rs.getDouble("first_price3");
				first_price4=rs.getDouble("first_price4");
				
				selectDateChange.setString(1, first_train_num);
				selectDateChange.setInt(2, first_from_stop);
				selectDateChange.setInt(3, first_to_stop);
				datechangeresult=selectDateChange.executeQuery();
				while (datechangeresult.next())
					datechange1=datechangeresult.getInt("dc");
				
				second_train_num=rs.getString("second_train_num");
				second_train_type=rs.getString("second_train_type");
				second_to_name=rs.getString("second_to_name");
				second_from_stop=rs.getInt("second_from_stop");
				second_to_stop=rs.getInt("second_to_stop");
				second_depart=rs.getString("second_leave");
				second_arrive=rs.getString("second_arrive");
				second_seat1=rs.getInt("second_seat1");
				second_seat2=rs.getInt("second_seat2");
				second_seat3=rs.getInt("second_seat3");
				second_seat4=rs.getInt("second_seat4");
				second_price1=rs.getDouble("second_price1");
				second_price2=rs.getDouble("second_price2");
				second_price3=rs.getDouble("second_price3");
				second_price4=rs.getDouble("second_price4");
				
				selectDateChange.setString(1, second_train_num);
				selectDateChange.setInt(2, second_from_stop);
				selectDateChange.setInt(3, second_to_stop);
				datechangeresult=selectDateChange.executeQuery();
				while (datechangeresult.next())
					datechange2=datechangeresult.getInt("dc");
				
				total_time=rs.getString("total_time");
				total_lowest_price=rs.getDouble("total_lowest_price");
				
				pathlist.add(new PathQuery(date, datechange1, datechange2, first_train_num, first_train_type, first_from_name, first_to_name, first_from_stop, first_to_stop, first_depart, first_arrive, first_seat1, first_seat2, first_seat3, first_seat4, first_price1, first_price2, first_price3, first_price4, 
						second_train_num, second_train_type, second_to_name, second_from_stop, second_to_stop, second_depart, second_arrive, second_seat1, second_seat2, second_seat3, second_seat4, second_price1, second_price2, second_price3, second_price4, total_time, total_lowest_price));
			}
			
			if (pathlist.isEmpty())
				System.out.println("本页无信息");
			else {
				System.out.println("(第"+page+"页)");
				System.out.println("最少换乘, 为您查询到以下线路: ");
				for (int i=0;i<pathlist.size();i++) {
					System.out.println("路线"+(i+1)+":");
					System.out.println(pathlist.get(i));
					System.out.println();
				}
				
				boolean ctrl=true;
				while (ctrl) {
					System.out.print("请选择路线编号查看详细信息: ");
					int num=scan.nextInt();
					while (num<1 || num>pathlist.size()) {
						System.out.print("无效编号, 请重新输入: ");
						num=scan.nextInt();
					}
					PathQuery pq=pathlist.get(num-1);
					pq.showDetail();
					
					System.out.print("是否需要订票? (y/n) ");
					String yn=scan.next();
					if (yn.equalsIgnoreCase("y") || yn.equalsIgnoreCase("yes"))
						reserveRecommendTicket(pq);
					
					System.out.print("查看其他车次? (y/n) ");
					String ctr=scan.next();
					ctrl=ctr.equalsIgnoreCase("y") || ctr.equalsIgnoreCase("yes");
				}
			}
			System.out.print("请输入跳转的页数 (0表示退出): ");
			page=scan.nextInt();
		}
	}
	
	public void fuzzySearchShortestTime() throws SQLException {
		System.out.print("请输入出发站: ");
		String start=scan.next();
		System.out.print("请输入到达站: ");
		String arrive=scan.next();
		
		System.out.println("请选择日期: ");
		ArrayList<String> dates=getDates();
		for (int i=0;i<dates.size();i++) 
			System.out.println((i+1)+". "+dates.get(i));
		int datenum=scan.nextInt();
		String date=dates.get(datenum-1);
		
		PreparedStatement aplp=conn.prepareStatement("select * from fuzzy_path_shortest_time(cast(? as date), ?, ?, ?);");
		ResultSet rs=null;
		aplp.setString(1, date);
		aplp.setString(2, start);
		aplp.setString(3, arrive);
		
		
		String first_train_num=null, first_train_type=null, first_from_name=null, first_to_name=null;
		int first_from_stop=-1, first_to_stop=-1;
		String first_depart=null, first_arrive=null;
		int first_seat1=-1, first_seat2=-1, first_seat3=-1, first_seat4=-1;
		double first_price1=-1, first_price2=-1, first_price3=-1, first_price4=-1;
		
		String second_train_num=null, second_train_type=null, second_to_name=null;
		int second_from_stop=-1, second_to_stop=-1;
		String second_depart=null, second_arrive=null;
		int second_seat1=-1, second_seat2=-1, second_seat3=-1, second_seat4=-1;
		double second_price1=-1, second_price2=-1, second_price3=-1, second_price4=-1;
		
		int datechange1=-1, datechange2=-1;
		
		String total_time=null;
		double total_lowest_price=-1;
		
		int page=1;
		while (page!=0) {
			ArrayList<PathQuery> pathlist=null;
			
			aplp.setInt(4, page);
			pathlist=new ArrayList<>();
			rs=aplp.executeQuery();
			PreparedStatement selectDateChange=conn.prepareStatement("select date_change(?, ?, ?) as dc;");
			ResultSet datechangeresult=null;
			while (rs.next()) {
				first_train_num=rs.getString("first_train_num");
				first_train_type=rs.getString("first_train_type");
				first_from_name=rs.getString("first_from_name");
				first_to_name=rs.getString("first_to_name");
				first_from_stop=rs.getInt("first_from_stop");
				first_to_stop=rs.getInt("first_to_stop");
				first_depart=rs.getString("first_depart");
				first_arrive=rs.getString("first_arrive");
				first_seat1=rs.getInt("first_seat1");
				first_seat2=rs.getInt("first_seat2");
				first_seat3=rs.getInt("first_seat3");
				first_seat4=rs.getInt("first_seat4");
				first_price1=rs.getDouble("first_price1");
				first_price2=rs.getDouble("first_price2");
				first_price3=rs.getDouble("first_price3");
				first_price4=rs.getDouble("first_price4");
				
				selectDateChange.setString(1, first_train_num);
				selectDateChange.setInt(2, first_from_stop);
				selectDateChange.setInt(3, first_to_stop);
				datechangeresult=selectDateChange.executeQuery();
				while (datechangeresult.next())
					datechange1=datechangeresult.getInt("dc");
				
				second_train_num=rs.getString("second_train_num");
				second_train_type=rs.getString("second_train_type");
				second_to_name=rs.getString("second_to_name");
				second_from_stop=rs.getInt("second_from_stop");
				second_to_stop=rs.getInt("second_to_stop");
				second_depart=rs.getString("second_leave");
				second_arrive=rs.getString("second_arrive");
				second_seat1=rs.getInt("second_seat1");
				second_seat2=rs.getInt("second_seat2");
				second_seat3=rs.getInt("second_seat3");
				second_seat4=rs.getInt("second_seat4");
				second_price1=rs.getDouble("second_price1");
				second_price2=rs.getDouble("second_price2");
				second_price3=rs.getDouble("second_price3");
				second_price4=rs.getDouble("second_price4");
				
				selectDateChange.setString(1, second_train_num);
				selectDateChange.setInt(2, second_from_stop);
				selectDateChange.setInt(3, second_to_stop);
				datechangeresult=selectDateChange.executeQuery();
				while (datechangeresult.next())
					datechange2=datechangeresult.getInt("dc");
				
				total_time=rs.getString("total_time");
				total_lowest_price=rs.getDouble("total_lowest_price");
				
				pathlist.add(new PathQuery(date, datechange1, datechange2, first_train_num, first_train_type, first_from_name, first_to_name, first_from_stop, first_to_stop, first_depart, first_arrive, first_seat1, first_seat2, first_seat3, first_seat4, first_price1, first_price2, first_price3, first_price4, 
						second_train_num, second_train_type, second_to_name, second_from_stop, second_to_stop, second_depart, second_arrive, second_seat1, second_seat2, second_seat3, second_seat4, second_price1, second_price2, second_price3, second_price4, total_time, total_lowest_price));
			}
			
			if (pathlist.isEmpty())
				System.out.println("本页无信息");
			else {
				System.out.println("(第"+page+"页)");
				System.out.println("最短用时, 为您查询到以下线路: ");
				for (int i=0;i<pathlist.size();i++) {
					System.out.println("路线"+(i+1)+":");
					System.out.println(pathlist.get(i));
					System.out.println();
				}
				
				boolean ctrl=true;
				while (ctrl) {
					System.out.print("请选择路线编号查看详细信息: ");
					int num=scan.nextInt();
					while (num<1 || num>pathlist.size()) {
						System.out.print("无效编号, 请重新输入: ");
						num=scan.nextInt();
					}
					PathQuery pq=pathlist.get(num-1);
					pq.showDetail();
					
					System.out.print("是否需要订票? (y/n) ");
					String yn=scan.next();
					if (yn.equalsIgnoreCase("y") || yn.equalsIgnoreCase("yes"))
						reserveRecommendTicket(pq);
					
					System.out.print("查看其他车次? (y/n) ");
					String ctr=scan.next();
					ctrl=ctr.equalsIgnoreCase("y") || ctr.equalsIgnoreCase("yes");
				}
			}
			System.out.print("请输入跳转的页数 (0表示退出): ");
			page=scan.nextInt();
		}
	}
	
	public void fuzzySearchRecommend() throws SQLException {
		System.out.print("请输入出发站: ");
		String start=scan.next();
		System.out.print("请输入到达站: ");
		String arrive=scan.next();
		
		System.out.println("请选择日期: ");
		ArrayList<String> dates=getDates();
		for (int i=0;i<dates.size();i++) 
			System.out.println((i+1)+". "+dates.get(i));
		int datenum=scan.nextInt();
		String date=dates.get(datenum-1);
		
		PreparedStatement aplp=conn.prepareStatement("select * from fuzzy_path_recommend(cast(? as date), ?, ?, ?);");
		ResultSet rs=null;
		aplp.setString(1, date);
		aplp.setString(2, start);
		aplp.setString(3, arrive);
		
		
		String first_train_num=null, first_train_type=null, first_from_name=null, first_to_name=null;
		int first_from_stop=-1, first_to_stop=-1;
		String first_depart=null, first_arrive=null;
		int first_seat1=-1, first_seat2=-1, first_seat3=-1, first_seat4=-1;
		double first_price1=-1, first_price2=-1, first_price3=-1, first_price4=-1;
		
		String second_train_num=null, second_train_type=null, second_to_name=null;
		int second_from_stop=-1, second_to_stop=-1;
		String second_depart=null, second_arrive=null;
		int second_seat1=-1, second_seat2=-1, second_seat3=-1, second_seat4=-1;
		double second_price1=-1, second_price2=-1, second_price3=-1, second_price4=-1;
		
		int datechange1=-1, datechange2=-1;
		
		String total_time=null;
		double total_lowest_price=-1;
		
		int page=1;
		while (page!=0) {
			ArrayList<PathQuery> pathlist=null;
			
			aplp.setInt(4, page);
			pathlist=new ArrayList<>();
			rs=aplp.executeQuery();
			PreparedStatement selectDateChange=conn.prepareStatement("select date_change(?, ?, ?) as dc;");
			ResultSet datechangeresult=null;
			while (rs.next()) {
				first_train_num=rs.getString("first_train_num");
				first_train_type=rs.getString("first_train_type");
				first_from_name=rs.getString("first_from_name");
				first_to_name=rs.getString("first_to_name");
				first_from_stop=rs.getInt("first_from_stop");
				first_to_stop=rs.getInt("first_to_stop");
				first_depart=rs.getString("first_depart");
				first_arrive=rs.getString("first_arrive");
				first_seat1=rs.getInt("first_seat1");
				first_seat2=rs.getInt("first_seat2");
				first_seat3=rs.getInt("first_seat3");
				first_seat4=rs.getInt("first_seat4");
				first_price1=rs.getDouble("first_price1");
				first_price2=rs.getDouble("first_price2");
				first_price3=rs.getDouble("first_price3");
				first_price4=rs.getDouble("first_price4");
				
				selectDateChange.setString(1, first_train_num);
				selectDateChange.setInt(2, first_from_stop);
				selectDateChange.setInt(3, first_to_stop);
				datechangeresult=selectDateChange.executeQuery();
				while (datechangeresult.next())
					datechange1=datechangeresult.getInt("dc");
				
				second_train_num=rs.getString("second_train_num");
				second_train_type=rs.getString("second_train_type");
				second_to_name=rs.getString("second_to_name");
				second_from_stop=rs.getInt("second_from_stop");
				second_to_stop=rs.getInt("second_to_stop");
				second_depart=rs.getString("second_leave");
				second_arrive=rs.getString("second_arrive");
				second_seat1=rs.getInt("second_seat1");
				second_seat2=rs.getInt("second_seat2");
				second_seat3=rs.getInt("second_seat3");
				second_seat4=rs.getInt("second_seat4");
				second_price1=rs.getDouble("second_price1");
				second_price2=rs.getDouble("second_price2");
				second_price3=rs.getDouble("second_price3");
				second_price4=rs.getDouble("second_price4");
				
				selectDateChange.setString(1, second_train_num);
				selectDateChange.setInt(2, second_from_stop);
				selectDateChange.setInt(3, second_to_stop);
				datechangeresult=selectDateChange.executeQuery();
				while (datechangeresult.next())
					datechange2=datechangeresult.getInt("dc");
				
				total_time=rs.getString("total_time");
				total_lowest_price=rs.getDouble("total_lowest_price");
				
				pathlist.add(new PathQuery(date, datechange1, datechange2, first_train_num, first_train_type, first_from_name, first_to_name, first_from_stop, first_to_stop, first_depart, first_arrive, first_seat1, first_seat2, first_seat3, first_seat4, first_price1, first_price2, first_price3, first_price4, 
						second_train_num, second_train_type, second_to_name, second_from_stop, second_to_stop, second_depart, second_arrive, second_seat1, second_seat2, second_seat3, second_seat4, second_price1, second_price2, second_price3, second_price4, total_time, total_lowest_price));
			}
			
			if (pathlist.isEmpty())
				System.out.println("本页无信息");
			else {
				System.out.println("(第"+page+"页)");
				System.out.println("系统推荐路线, 为您查询到以下线路: ");
				for (int i=0;i<pathlist.size();i++) {
					System.out.println("路线"+(i+1)+":");
					System.out.println(pathlist.get(i));
					System.out.println();
				}
				
				boolean ctrl=true;
				while (ctrl) {
					System.out.print("请选择路线编号查看详细信息: ");
					int num=scan.nextInt();
					while (num<1 || num>pathlist.size()) {
						System.out.print("无效编号, 请重新输入: ");
						num=scan.nextInt();
					}
					PathQuery pq=pathlist.get(num-1);
					pq.showDetail();
					
					System.out.print("是否需要订票? (y/n) ");
					String yn=scan.next();
					if (yn.equalsIgnoreCase("y") || yn.equalsIgnoreCase("yes"))
						reserveRecommendTicket(pq);
					
					System.out.print("查看其他车次? (y/n) ");
					String ctr=scan.next();
					ctrl=ctr.equalsIgnoreCase("y") || ctr.equalsIgnoreCase("yes");
				}
			}
			System.out.print("请输入跳转的页数 (0表示退出): ");
			page=scan.nextInt();
		}
	}
	
}
