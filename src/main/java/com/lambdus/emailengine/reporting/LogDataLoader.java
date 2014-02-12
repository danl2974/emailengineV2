package com.lambdus.emailengine.reporting;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.jboss.logging.Logger;

public class LogDataLoader {
	
	private static final Logger log = Logger.getLogger(LogDataLoader.class.getName());
	
	private static String azureConnection = "jdbc:sqlserver://v8st4k97ey.database.windows.net:1433;database=email_engine;user=email_engine@v8st4k97ey;password=!Lambdus2200;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
	
	
	static public void loadSuccessData(ArrayList<EmailSuccess> successList){
		
		CallableStatement callableStatement = null;
		ResultSet rs = null;
		
		try{
		  String sproc = "{call test.LoadEmailSuccess(?,?,?,?,?,?)}";
		  Connection con = DriverManager.getConnection(azureConnection);
		  callableStatement = con.prepareCall(sproc);
		  
		  for (EmailSuccess es : successList){
		      callableStatement.setTimestamp(1, es.timestamp);
		      callableStatement.setString(2, es.toAddress);
		      callableStatement.setString(3, es.outboundIP);
		      callableStatement.setString(4, es.outboundHost);
		      callableStatement.setString(5, es.remoteIP);
		      callableStatement.setString(6, es.remoteHost);
		      callableStatement.addBatch();
		  }
	      try{
		       callableStatement.executeBatch();
		      }catch(Exception e){log.error(e.getMessage());}
		 
		  con.close();
		}
		catch(SQLException sqle){
			log.error(sqle.getMessage());
		}
	}
	
	
	
	static public void loadBounceData(ArrayList<EmailBounce> bounceList){
		
		CallableStatement callableStatement = null;
		ResultSet rs = null;
		
		try{
		  String sproc = "{call test.LoadEmailBounce(?,?,?,?,?,?,?)}";
		  Connection con = DriverManager.getConnection(azureConnection);
		  callableStatement = con.prepareCall(sproc);
		  
		  for (EmailBounce eb : bounceList){
		      callableStatement.setTimestamp(1, eb.timestamp);
		      callableStatement.setString(2, eb.toAddress);
		      callableStatement.setString(3, eb.outboundIP);
		      callableStatement.setString(4, eb.outboundHost);
		      callableStatement.setString(5, eb.remoteIP);
		      callableStatement.setString(6, eb.remoteHost);
		      callableStatement.setString(7, eb.ispResponse);
		      callableStatement.addBatch();
		  }
	      try{
		       callableStatement.executeBatch();
		      }catch(Exception e){log.error(e.getMessage());}
		 
		  con.close();
		}
		catch(SQLException sqle){
			log.error(sqle.getMessage());
		}
	}
	

}
