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
    	while (control) {
	    	System.out.print("Enter your name: ");
			name=scan.next();
			System.out.print("Enter password: ");
			pw=scan.next();
			
			PreparedStatement stmt=conn.prepareStatement("select type from users where user_name=? and password=?;");
			stmt.setString(1, name);
			stmt.setString(2, pw);
			
			rs=stmt.executeQuery();
			if (!rs.next()) {
				System.out.println("Wrong name or password, please retry.");
				continue;
			}
			else {
				System.out.println(name+", welcome back!");
				control=false;
				stmt.close();
			}
    	}
    	String t=rs.getString("type");
    	rs.close();
		if (t.equals("A"))
			return new User(name, UserType.ADMIN);
		return new User(name, UserType.PASSENGER);
    }
	
	public static User signUp() throws Exception {
		boolean control=true;
		String name=null;
		while (control) {
			System.out.println("-----Creat new user-----");
			System.out.print("Enter your name: ");
			name=scan.next();
			String pw=null;
			boolean control2=true;
			while (control2) {
				System.out.print("Enter your password: ");
				pw=scan.next();
				System.out.print("Confirm your password: ");
				String temp=scan.next();
				if (!temp.equals(pw)) {
					System.out.println("Wrong password.");
					continue;
				}
				else
					control2=false;
			}
			
			PreparedStatement stmt=conn.prepareStatement("insert into users (user_name, password, type) values (?, ?, 'P');");
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
		}
		System.out.println("Welcome, "+name+"!");
		return new User(name, UserType.PASSENGER);
	}

}
