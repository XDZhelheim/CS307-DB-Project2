package Project2_12306;

import java.util.*;

public class Main {
	static Scanner scan=new Scanner(System.in);
	
    public static void main(String[] args) throws Exception {
    	System.out.println("欢迎！");
    	System.out.println("1. 登录");
    	System.out.println("2. 注册");
    	User current_user=null;
		int control=scan.nextInt();
		if (control==1)
			current_user=User.login();
		else if (control==2)
			current_user=User.signUp();
		current_user.queryTrain("北京", "深圳");
	}
    
}
