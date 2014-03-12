package com.lambdus.emailengine;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.logging.Logger;


//@EJB(name="batchCampaignController")
@Stateful
@Remote(IBatchCampaignController.class)
public class BatchCampaignController implements IBatchCampaignController {
	
	private static Logger log = Logger.getLogger(BatchCampaignController.class.getName());
	
	private int templateId;
	
	private List<Integer> targetIds;
	
	private int targetId;
	
	//MAKE CONFIG
    private static String jdbcHandle = "jdbc:mysql://localhost:3306/email_engine";
	private static String dbusername = "dan";
	private static String dbpassword = "lambdus2200";
	
	
	@Override
	public void startCampaign(){
		
		String uuid = UUID.randomUUID().toString();
		//String association = resolveAssociation(this.targetId);
		log.info("startCampaign targets size " + this.targetIds.size());
		String association = resolveAssociation(this.targetIds.get(0));
		
		Integer result = 0;
		
		for(Integer target : this.targetIds){
		  log.info("Target in for loop with startCampaign " + target);	
          BatchRequest request = new BatchRequest();
          request.setTargetId(target);
          request.setTemplateId(this.templateId);
          request.setUuid(uuid);
        
          BatchProcessor bp = new BatchProcessor(request);
          //FutureTask futureTask = new FutureTask(bp);
          ExecutorService executorService = Executors.newSingleThreadExecutor();
          //executorService.submit(futureTask);
 
          //Integer result = 0;
          FutureTask<Integer> submittedBatchTask = (FutureTask<Integer>) executorService.submit(bp);
        
           while(!submittedBatchTask.isDone()){
            try{result += submittedBatchTask.get();}
            catch (InterruptedException ie) {log.error(ie.getMessage());return;}
            catch (ExecutionException ee) {log.error(ee.getMessage());return;}
            }
           executorService.shutdown(); 
	     } 
    	 addBatchCampaignMonitorData(result, addNewBatchCampaignMonitor(uuid, association));
		 //executorService.shutdown();
	}
	
	
	private String addNewBatchCampaignMonitor(String uuid, String association){
	
		Connection con = null;
		CallableStatement callableStatement = null;
		SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("EST"));
		String startTime = sdf.format(new Date());
	    try {
	    	 Class.forName("com.mysql.jdbc.Driver");
		     con = DriverManager.getConnection(jdbcHandle, dbusername, dbpassword);
		     
		     String sproc = "{call addBatchCampaign(?,?,?)}";
		     callableStatement = con.prepareCall(sproc);
		     callableStatement.setString(1, uuid);
		     callableStatement.setString(2, startTime);
		     callableStatement.setString(3, association);
		     callableStatement.executeUpdate();
		     callableStatement.close();
		     con.close();  
	    }
	    catch(Exception e){log.error(e.getMessage());}
	    return uuid;
	}
	
	private boolean addBatchCampaignMonitorData(int total, String uuid){
		
		Connection con = null;
		CallableStatement callableStatement = null;
	    try {
	    	 Class.forName("com.mysql.jdbc.Driver");
		     con = DriverManager.getConnection(jdbcHandle, dbusername, dbpassword);
		     String sproc = "{call addBatchCampaignTotal(?,?)}";
		     callableStatement = con.prepareCall(sproc);
		     callableStatement.setString(1, uuid);
		     callableStatement.setInt(2, total);
		     boolean ex = callableStatement.execute();
		     con.close();
		     callableStatement.close();
		     return ex;
	    }
	    catch(Exception e){log.error(e.getMessage()); return false;}
	}	
	
	
	
	 public static String resolveAssociation(int targetId)
	 {
		ResultSet rs = null;
		Connection con = null;
		CallableStatement callableStatement = null;
		String association = "";
	    try {
	    	 Class.forName("com.mysql.jdbc.Driver");
	    	 log.info("Before fetTarget JDBC conn call");
		     con = DriverManager.getConnection(jdbcHandle, dbusername, dbpassword);
		     
		     String sproc = "{call getTargetById(?)}";
		     callableStatement = con.prepareCall(sproc);
		     callableStatement.setInt(1, targetId);
		     boolean ex = callableStatement.execute();
		     rs = callableStatement.getResultSet();
		     if(ex){
		            while(rs.next()){association = rs.getString("association");}
		           }
	         }
	     catch(Exception e){log.error(e.getMessage());}
	    
	     return association;
		     
	   }	     
	
	
	
	@Override
    public void setTargetId(int targetId)
    {
            this.targetId = targetId;
    }
	
	@Override
    public void setTemplateId(int templateId)
    {
            this.templateId = templateId;
    }
	
    
    public int getTargetId()
    {
            return this.targetId;
    }
    
    public int getTemplateId()
    {
            return this.templateId;
    }
    
    @Override
    public void setTargetIds(List<Integer> targetIds)
    {
            this.targetIds = targetIds;
    }
    
    public List<Integer> getTargetIds()
    {
            return this.targetIds;
    }	
	

}
