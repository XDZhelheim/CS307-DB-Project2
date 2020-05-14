package Project2_12306;

import java.util.*;
import Project2_12306.User.UserType;

public class Main {
	static Scanner scan=new Scanner(System.in);
	static User current_user=null;
	static int control=-1;
	static ArrayList<TrainQuery> trainlist=null;
	static ArrayList<OrderQuery> orderlist=null;
	
    public static void main(String[] args) throws Exception {
    	while (control==-1) {
	    	System.out.println("欢迎！");
	    	System.out.println("1. 登录");
	    	System.out.println("2. 注册");
	    	System.out.println("0. 退出");
			control=scan.nextInt();
			if (control==1)
				current_user=User.login();
			else if (control==2)
				current_user=User.signUp();
			else if (control==0) {
				System.out.println("感谢使用，再见！");
				User.closeConnection();
				System.exit(0);
			}
			control=-2;
			if (current_user.getType()==UserType.PASSENGER) {
				while (control==-2) {
					System.out.println("1. 查询车次");
			    	System.out.println("2. 订票");
			    	System.out.println("3. 查询订单");
			    	System.out.println("4. 取消订单");
			    	System.out.println("0. 注销");
			    	control=scan.nextInt();
			    	if (control==0)
			    		User.logout();
			    	else if (control==1) {
			    		System.out.print("请输入出发站: ");
			    		String start=scan.next();
			    		System.out.print("请输入到达站: ");
			    		String arrive=scan.next();
			    		trainlist=current_user.queryTrain(start, arrive);
			    		System.out.print("是否需要订票? (y/n) ");
			    		String yn=scan.next();
			    		if (yn.equalsIgnoreCase("y") || yn.equalsIgnoreCase("yes"))
			    			current_user.reserveTicket(start, arrive, true, trainlist);
			    		Thread.sleep(1000);
			    		control=-2;
			    	}
			    	else if (control==2) {
			    		System.out.print("请输入出发站: ");
			    		String start=scan.next();
			    		System.out.print("请输入到达站: ");
			    		String arrive=scan.next();
			    		current_user.reserveTicket(start, arrive, false, null);
			    		Thread.sleep(1000);
			    		control=-2;
			    	}
			    	else if (control==3) {
			    		orderlist=current_user.queryOrder();
			    		System.out.print("是否需要取消订单? (y/n) ");
			    		String yn=scan.next();
		    			if (yn.equalsIgnoreCase("y") || yn.equalsIgnoreCase("yes"))
		    				current_user.cancelOrder(true, orderlist);
		    			Thread.sleep(1000);
			    		control=-2;
			    	}
			    	else if (control==4) {
			    		current_user.cancelOrder(false, null);
			    		Thread.sleep(1000);
			    		control=-2;
			    	}
				}
			}
			
    	}
    	
	}
}
