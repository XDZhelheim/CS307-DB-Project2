package Project2_12306;

import java.util.*;

public class Main {
	static Scanner scan=new Scanner(System.in);
	static User current_user=null;
	static int control=-1;
	
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
			current_user.queryTrain("北京", "深圳");
			User.logout();
    	}
	}
    
}
