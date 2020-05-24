package Project2_12306;

import java.sql.*;

import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.admin.SnapshotIF;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;

public class ProxoolConnectionPool {  
   private static String config = "./src/Project2_12306/ProxoolConfig.xml";
   
   static ProxoolConnectionPool connector=new ProxoolConnectionPool();
 
   public ProxoolConnectionPool() {  
	   try {  
           JAXPConfigurator.configure(config, false);  
       } catch (ProxoolException ex) {  
           ex.printStackTrace();  
       }   
   }  
 
 
   public Connection getConn() {  
	   Connection conn=null;
       try {
           conn=DriverManager.getConnection("proxool.postgres");
       } catch (SQLException e) {
           e.printStackTrace();
       }
       return conn;
   }  
     
   	public static String getConnectState() {
   		try {
   			SnapshotIF snapshotIF = ProxoolFacade.getSnapshot("postgres", true);
   			int curActiveCnt = snapshotIF.getActiveConnectionCount();
   			int availableCnt = snapshotIF.getAvailableConnectionCount();
   			int maxCnt = snapshotIF.getMaximumConnectionCount();
   			return String.format("--- Active:%d\tAvailable:%d  \tMax:%d ---\n",
                   curActiveCnt, availableCnt, maxCnt);
   		} catch (ProxoolException e) {
           e.printStackTrace();
   		}
   		return "visit error";
   	}
}  

