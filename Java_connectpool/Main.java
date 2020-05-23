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
					System.out.println("-------------");
					System.out.println("1. 查询车次");
					System.out.println("2. 查询单列车");
			    	System.out.println("3. 订票");
			    	System.out.println("4. 查询订单");
			    	System.out.println("5. 取消订单");
			    	System.out.println("6. 查询推荐路线");
			    	System.out.println("0. 注销");
			    	control=scan.nextInt();
			    	if (control==0)
			    		User.logout();
			    	else if (control==1) {
			    		System.out.print("请输入出发站: ");
			    		String start=scan.next();
			    		System.out.print("请输入到达站: ");
			    		String arrive=scan.next();
			    		current_user.queryTrain_reserveTicket(start, arrive, false);
			    		control=-2;
			    	}
			    	else if (control==2) {
			    		current_user.queryTrainInformation();
			    		control=-2;
			    	}
			    	else if (control==3) {
			    		System.out.print("请输入出发站: ");
			    		String start=scan.next();
			    		System.out.print("请输入到达站: ");
			    		String arrive=scan.next();
			    		current_user.queryTrain_reserveTicket(start, arrive, true);
			    		control=-2;
			    	}
			    	else if (control==4) {
			    		orderlist=current_user.queryOrder();
			    		System.out.print("是否需要取消订单? (y/n) ");
			    		String yn=scan.next();
		    			if (yn.equalsIgnoreCase("y") || yn.equalsIgnoreCase("yes"))
		    				current_user.cancelOrder(true, orderlist);
			    		control=-2;
			    	}
			    	else if (control==5) {
			    		current_user.cancelOrder(false, null);
			    		control=-2;
			    	}
			    	else if (control==6) {
			    		System.out.println("1. 精确查找");
			    		System.out.println("2. 模糊查找");
			    		int ct1=scan.nextInt();
		    			System.out.println("1. 最低费用");
		    			System.out.println("2. 最少换乘");
		    			System.out.println("3. 最短用时");
		    			System.out.println("4. 系统推荐");
		    			int ct2=scan.nextInt();
		    			if (ct1==1) {
		    				if (ct2==1)
		    					current_user.accurateSearchLowestPrice();
		    				if (ct2==2)
		    					current_user.accurateSearchMinTransfer();
		    				if (ct2==3)
		    					current_user.accurateSearchShortestTime();
		    				if (ct2==4)
		    					current_user.accurateSearchRecommend();
		    			}
		    			else if (ct1==2) {
		    				if (ct2==1)
		    					current_user.fuzzySearchLowestPrice();
		    				if (ct2==2)
		    					current_user.fuzzySearchMinTransfer();
		    				if (ct2==3)
		    					current_user.fuzzySearchShortestTime();
		    				if (ct2==4)
		    					current_user.fuzzySearchRecommend();
		    			}
		    			
		    			control=-2;
			    	}
				}
			}
			else {
				while (control==-2) {
					System.out.println("1. 添加列车");
					System.out.println("2. 删除列车");
					System.out.println("3. 添加列车站点");
					System.out.println("4. 删除列车站点");
			    	System.out.println("0. 注销");
			    	control=scan.nextInt();
			    	if (control==0)
			    		User.logout();
			    	else if (control==1) {
			    		current_user.addTrain();
			    		control=-2;
			    	}
			    	else if (control==2) {
			    		current_user.deleteTrain();
			    		control=-2;
			    	}
			    	else if (control==3) {
			    		current_user.insertSchedule();
			    		control=-2;
			    	}
			    	else if (control==4) {
			    		current_user.deleteSchedule();
			    		control=-2;
			    	}
				}
			}
			
    	}
    	
	}
}
