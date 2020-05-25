package Project2_12306;

import java.sql.SQLException;
import java.util.ArrayList;

public class PathQuery {
	String date;
	String first_train_num, first_train_type, first_from_name, first_to_name;
	int first_from_stop, first_to_stop;
	String first_depart, first_arrive;
	int first_seat1, first_seat2, first_seat3, first_seat4;
	double first_price1, first_price2, first_price3, first_price4;
	
	String second_train_num, second_train_type, second_to_name;
	int second_from_stop, second_to_stop;
	String second_depart, second_arrive;
	int second_seat1, second_seat2, second_seat3, second_seat4;
	double second_price1, second_price2, second_price3, second_price4;
	
	int datechange1, datechange2;
	
	String total_time;
	double total_lowest_price;
	
	static ArrayList<String> seats;
	
	static {
		try {
			seats=User.getSeats();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public PathQuery(String date, int datechange1, int datechange2, String first_train_num, String first_train_type, String first_from_name, String first_to_name,
			int first_from_stop, int first_to_stop, String first_depart, String first_arrive, int first_seat1,
			int first_seat2, int first_seat3, int first_seat4, double first_price1, double first_price2,
			double first_price3, double first_price4, String second_train_num, String second_train_type,
			String second_to_name, int second_from_stop, int second_to_stop, String second_depart, String second_arrive,
			int second_seat1, int second_seat2, int second_seat3, int second_seat4, double second_price1,
			double second_price2, double second_price3, double second_price4, String total_time,
			double total_lowest_price) {
		this.date=date;
		this.datechange1=datechange1;
		this.datechange2=datechange2;
		this.first_train_num = first_train_num;
		this.first_train_type = first_train_type;
		this.first_from_name = first_from_name;
		this.first_to_name = first_to_name;
		this.first_from_stop = first_from_stop;
		this.first_to_stop = first_to_stop;
		this.first_depart = first_depart;
		this.first_arrive = first_arrive;
		this.first_seat1 = first_seat1;
		this.first_seat2 = first_seat2;
		this.first_seat3 = first_seat3;
		this.first_seat4 = first_seat4;
		this.first_price1 = first_price1;
		this.first_price2 = first_price2;
		this.first_price3 = first_price3;
		this.first_price4 = first_price4;
		this.second_train_num = second_train_num;
		this.second_train_type = second_train_type;
		this.second_to_name = second_to_name;
		this.second_from_stop = second_from_stop;
		this.second_to_stop = second_to_stop;
		this.second_depart = second_depart;
		this.second_arrive = second_arrive;
		this.second_seat1 = second_seat1;
		this.second_seat2 = second_seat2;
		this.second_seat3 = second_seat3;
		this.second_seat4 = second_seat4;
		this.second_price1 = second_price1;
		this.second_price2 = second_price2;
		this.second_price3 = second_price3;
		this.second_price4 = second_price4;
		this.total_time = total_time;
		this.total_lowest_price = total_lowest_price;
	}
	
	@Override
	public String toString() {
		String temp1=first_train_type+first_train_num;
		if (temp1.length()<=8) {
			int x=8-temp1.length();
			for (int k=0;k<=x;k++)
				temp1+=" ";
		}
		String temp2=second_train_type+second_train_num;
		if (temp2.length()<=8) {
			int x=8-temp2.length();
			for (int k=0;k<=x;k++)
				temp2+=" ";
		}
		String s1=temp1+"\t出发站: "+first_from_name+"\t到达站: "+first_to_name+"\t出发时间: "+first_depart+"\t到达时间: "+first_arrive;
		if (datechange1>0)
			s1+="(+"+datechange1+")";
		String s2=temp2+"\t出发站: "+first_to_name+"\t到达站: "+second_to_name+"\t出发时间: "+second_depart+"\t到达时间: "+second_arrive;
		if (datechange2>0)
			s2+="(+"+datechange2+")";
		if (second_from_stop!=0)
			s1+="\n换乘\n"+s2;
		s1+="\n日期:"+date+"\t总用时: "+total_time+"\t总最低票价: "+total_lowest_price;
		return s1;
	}
	
	public void showDetail() {
		System.out.println(first_train_num+"车票信息: ");
		System.out.println("\t票价: ");
		System.out.print("\t\t"+seats.get(0)+": "+first_price1);
		System.out.print("\t\t"+seats.get(1)+": "+first_price2);
		System.out.print("\t\t"+seats.get(2)+": "+first_price3);
		System.out.print("\t\t"+seats.get(3)+": "+first_price4);
		System.out.println();
		System.out.println("\t"+date+" 余票: ");
		System.out.print("\t\t"+seats.get(0)+": "+first_seat1);
		System.out.print("\t\t"+seats.get(1)+": "+first_seat2);
		System.out.print("\t\t"+seats.get(2)+": "+first_seat3);
		System.out.print("\t\t\t"+seats.get(3)+": "+first_seat4);
		System.out.println();
		if (second_from_stop!=0) {
			System.out.println(second_train_num+"车票信息: ");
			System.out.println("\t票价: ");
			System.out.print("\t\t"+seats.get(0)+": "+second_price1);
			System.out.print("\t\t"+seats.get(1)+": "+second_price2);
			System.out.print("\t\t"+seats.get(2)+": "+second_price3);
			System.out.print("\t\t"+seats.get(3)+": "+second_price4);
			System.out.println();
			System.out.println("\t"+date+" 余票: ");
			System.out.print("\t\t"+seats.get(0)+": "+second_seat1);
			System.out.print("\t\t"+seats.get(1)+": "+second_seat2);
			System.out.print("\t\t"+seats.get(2)+": "+second_seat3);
			System.out.print("\t\t\t"+seats.get(3)+": "+second_seat4);
			System.out.println();
		}
	}
}
