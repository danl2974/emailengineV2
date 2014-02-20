package com.lambdus.emailengine.persistence;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.jboss.logging.Logger;

import com.lambdus.emailengine.BatchRequest;


public class EventDropLocal {
	
	private static Logger log = Logger.getLogger(EventDropLocal.class.getName());
	
    private static String jdbcHandle = "jdbc:mysql://localhost:3306/email_engine";
	private static String dbusername = "dan";
	private static String dbpassword = "lambdus2200";
	
	
	static public boolean addBatchCampaignDetail(TemplatePersist templatePersist, BatchRequest batchRequest){
		
		Connection con = null;
		CallableStatement callableStatement = null;
	    try {
	    	 Class.forName("com.mysql.jdbc.Driver");
		     con = DriverManager.getConnection(jdbcHandle, dbusername, dbpassword);
		     String sproc = "{call addBatchCampaignDetail(?,?,?,?,?,?)}";
		     callableStatement = con.prepareCall(sproc);
		     callableStatement.setString(1, batchRequest.getUuid());
		     callableStatement.setString(2, templatePersist.getSubjectline());
		     callableStatement.setInt(3, batchRequest.getTemplateId());
		     callableStatement.setInt(4, batchRequest.getTargetId());
		     callableStatement.setString(5, templatePersist.getFromname());
		     callableStatement.setString(6, templatePersist.getFromaddress());		     
		     boolean ex = callableStatement.execute();
		     con.close();
		     callableStatement.close();
		     return ex;
	    }
	    catch(SQLException sqle){log.error(sqle.getMessage()); return false;}
	    catch(Exception e){log.error(e.getMessage()); return false;}
	}	
	
	
}
