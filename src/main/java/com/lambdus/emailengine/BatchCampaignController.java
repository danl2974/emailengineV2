package com.lambdus.emailengine;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	
	private int targetId;
	
	//MAKE CONFIG
    private static String jdbcHandle = "jdbc:mysql://localhost:3306/email_engine";
	private static String dbusername = "dan";
	private static String dbpassword = "lambdus2200";
	
	
	@Override
	public void startCampaign(){

        BatchRequest request = new BatchRequest();
        request.setTargetId(this.targetId);
        request.setTemplateId(this.templateId);
        
        BatchProcessor bp = new BatchProcessor(request);
        FutureTask<Integer> futureTask = new FutureTask<Integer>(bp);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        //executorService.submit(futureTask);
 
        String uuid = UUID.randomUUID().toString();
        Integer result = 0;
        FutureTask<Integer> submittedBatchTask = (FutureTask<Integer>) executorService.submit(futureTask);
        
         while(!submittedBatchTask.isDone()){
         try{result = submittedBatchTask.get();}
         catch (InterruptedException ie) {log.error(ie.getMessage());return;}
         catch (ExecutionException ee) {log.error(ee.getMessage());return;}
         }
         
    	 addBatchCampaignMonitorData(String.valueOf(result), addNewBatchCampaignMonitor(uuid));
		 executorService.shutdown();
	}
	
	
	private String addNewBatchCampaignMonitor(String uuid){
	
		Connection con = null;
		CallableStatement callableStatement = null;
		SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String startTime = sdf.format(new Date());
	    try {
	    	 Class.forName("com.mysql.jdbc.Driver");
		     con = DriverManager.getConnection(jdbcHandle, dbusername, dbpassword);
		     
		     String sproc = "{call addBatchCampaign(?,?)}";
		     callableStatement = con.prepareCall(sproc);
		     callableStatement.setString(1, uuid);
		     callableStatement.setString(2, startTime);
		     callableStatement.executeUpdate();
		     callableStatement.close();
		     con.close();    
	    }
	    catch(Exception e){log.error(e.getMessage());}
	    return uuid;
	}
	
	public static boolean addBatchCampaignMonitorData(String status, String uuid){
		
		Connection con = null;
		CallableStatement callableStatement = null;
	    try {
	    	 Class.forName("com.mysql.jdbc.Driver");
		     con = DriverManager.getConnection(jdbcHandle, dbusername, dbpassword);
		     String sproc = "{call addBatchCampaignMonitorData(?,?)}";
		     callableStatement = con.prepareCall(sproc);
		     callableStatement.setString(1, uuid);
		     callableStatement.setString(2, status);
		     boolean ex = callableStatement.execute();
		     con.close();
		     callableStatement.close();
		     return ex;
	    }
	    catch(Exception e){log.error(e.getMessage()); return false;}
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
    

}
