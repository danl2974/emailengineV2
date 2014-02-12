package com.lambdus.emailengine;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import org.jboss.logging.Logger;


public class BatchProcessor implements Callable<String> {
	
	 private static final Logger log = Logger.getLogger(BatchProcessor.class.getName());
	
	 private int targetId = 0;
	 private int templateId = 0;
	 BatchTarget batchtarget;
	 BatchRequest request;
	 
	 private static String jdbcHandle = "jdbc:mysql://localhost:3306/email_engine";
	 private static String dbusername = "dan";
	 private static String dbpassword = "lambdus2200";
	 
	 //Azure instance
	 private static String azureConnection = "jdbc:sqlserver://v8st4k97ey.database.windows.net:1433;database=email_engine;user=email_engine@v8st4k97ey;password=!Lambdus2200;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
	 
	
	 public BatchProcessor(BatchRequest request)
	 {
		 this.request = request;
	     this.targetId = request.getTargetId();
	     this.templateId = request.getTemplateId();
	     // HACK: Do BatchTarget config
	     this.batchtarget = new BatchTarget("email");
	 }
	
	 public String fetchTarget()
	 {
		ResultSet rs = null;
		Connection con = null;
		Statement st = null;
		CallableStatement callableStatement = null;
		String queryText = "";
	    try {
	    	 Class.forName("com.mysql.jdbc.Driver");
	    	 log.info("Before fetTarget JDBC conn call");
		     con = DriverManager.getConnection(jdbcHandle, dbusername, dbpassword);
		     
		     String sproc = "{call getTargetById(?)}";
		     callableStatement = con.prepareCall(sproc);
		     callableStatement.setInt(1, this.targetId);
		     //st = con.createStatement();
		     //String select = String.format("SELECT queryText FROM email_engine.targets WHERE id = %d;", this.targetId);
		     
		     try{
		     //rs = st.executeQuery(select);
		     boolean ex = callableStatement.execute();
		     log.info("execute on DB: " + String.valueOf(ex));
		     if (ex){
		    	 rs = callableStatement.getResultSet();
		     }
		       while(rs.next()){
		       queryText = rs.getString("queryText");
		       }
		     }
		     catch(Exception e){
		    	log.error(e.getMessage());
		     }
		     
		     con.close();
		     log.info("Query Text " + queryText);	     
		     
		} catch (SQLException e) {
			log.error(e.getMessage());
		} catch(Exception e){
			log.error(e.getMessage());
		}
	    
		
		 return queryText;
		 
	 }
	 
	 public HashMap<String,Object> processQuery(String query, BatchTarget batchtarget)
	 {
		log.info("Query " + query);
		ResultSet rs;
		HashMap<String,Object> userData = new HashMap<String,Object>();
		 try {
			 // Use this for production
			 //Connection con = DriverManager.getConnection(jdbcHandle, dbusername, dbpassword);
			 //Azure Test Conn String
			 Connection con = DriverManager.getConnection(azureConnection);
			 Statement stmt = con.createStatement();
			 rs = stmt.executeQuery(query);
			 
			 this.batchtarget.setFields(collectBatchGroupFields(rs));
			 
			 while (rs.next()) {
				 HashMap<String,String> fieldmap = new HashMap<String,String>();
				 if (batchtarget.getFields().size() > 0){
				    for (String f: batchtarget.getFields())
				      {
					    fieldmap.put(f, rs.getString(f));
				      }
				 }
				 userData.put(rs.getString(batchtarget.getEmailAddress()), fieldmap);
			 }
			 con.close();
		 } 
		 catch (SQLException e) {
				log.info(e.getMessage());
			}
	 
		 return userData;
	 }
	 
	 
	  private ArrayList<String> collectBatchGroupFields(ResultSet resultSet)
	  {
		  ArrayList<String> fields = new ArrayList<String>();
		  try {
			ResultSetMetaData rsmd = resultSet.getMetaData();
			for(int i = 1; i <= rsmd.getColumnCount(); i++){
				if(rsmd.getColumnName(i) != batchtarget.getEmailAddress()){
				   fields.add(rsmd.getColumnName(i));
				}
			}
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
		  
		return fields;  
	  }
	 
	 
	   @Override
	   public String call() throws Exception {
		    String targetQueryText = fetchTarget();
		    HashMap<String,Object> batchDirectiveHash = processQuery(targetQueryText, batchtarget);
		    BatchQueueProducer bqp = new BatchQueueProducer();
		    bqp.initialize(batchDirectiveHash, request);
		    int processed = bqp.processBatch();
		    String batchResultInfo = String.format("%s %s",Thread.currentThread().getName(), String.valueOf(processed) );
	        return batchResultInfo;
	    }
	 
	

}
