package com.lambdus.emailengine.reporting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.jboss.logging.Logger;


public class PostfixLogParser {
	
	private static final Logger log = Logger.getLogger(PostfixLogParser.class.getName());
	
	private static final String POSTFIX_LOG_PATH = "/var/log/mail.log";
	
	private static final String POSTFIX_SUCCESS_MARKER = "status=sent (250 ok dirdel)";
	
	private static final String POSTFIX_BOUNCE_MARKER = "status=bounced";
	
	static public ArrayList<EmailSuccess> processSuccess(){
		
		BufferedReader br = null;
		ArrayList<EmailSuccess> successList = new ArrayList<EmailSuccess>();
		try{
			String sLine;
		
		    br = new BufferedReader(new FileReader(POSTFIX_LOG_PATH));
		 
		    while ((sLine = br.readLine()) != null) 
		    {
		    	if(sLine.indexOf(POSTFIX_SUCCESS_MARKER) != -1){
		             EmailSuccess success = new EmailSuccess(); 		
		             success.toAddress = getToAddress(sLine);
		             success.timestamp = getTimestamp(sLine);
		             String[] remoteData = getRemoteData(sLine);
		             success.remoteHost = remoteData[0];
		             success.remoteIP = remoteData[1];
		             success.outboundHost = "";
		             success.outboundIP = "";
		             successList.add(success);
		             log.info("success added " + success.remoteIP + " " + success.timestamp);
		    	}
		    }
		    br.close();
	    } 
		
		catch (IOException e) {
		    log.error(e.getMessage());
	     } 
		
		return successList;
	
	}
	
	
	static public ArrayList<EmailBounce> processBounce(){
		
		BufferedReader br = null;
		ArrayList<EmailBounce> bounceList = new ArrayList<EmailBounce>();
		try{
			String sLine;
		
		    br = new BufferedReader(new FileReader(POSTFIX_LOG_PATH));
		 
		    while ((sLine = br.readLine()) != null) 
		    {
		    	if(sLine.indexOf(POSTFIX_BOUNCE_MARKER) != -1){
		             EmailBounce bounce = new EmailBounce(); 		
		             bounce.toAddress = getToAddress(sLine);
		             bounce.timestamp = getTimestamp(sLine);
		             String[] remoteData = getRemoteData(sLine);
		             bounce.remoteHost = remoteData[0];
		             bounce.remoteIP = remoteData[1];
		             bounce.outboundHost = "";
		             bounce.outboundIP = "";
		             bounce.ispResponse = getIspResponse(sLine);
		             bounceList.add(bounce);
		             log.info("bounce added " + bounce.ispResponse);
		    	}
		    }
		    br.close();
	    } 
		
		catch (IOException e) {
		    log.error(e.getMessage());
	     } 
		
		return bounceList;
	
	}	
	
	

	static private String getToAddress(String line){
		try{
		return line.split("to=<")[1].split(">")[0];
		}catch(Exception e) {return "";}
	}
	
	static private Timestamp getTimestamp(String line){
		Date date = new Date();
		String logdate = line.substring(0, 15);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM d HH:mm:ss");
		try{
		 date = sdf.parse(Calendar.getInstance().get(Calendar.YEAR) + " " + logdate);
		}catch(ParseException pe){log.error(pe.getMessage());}
		return new Timestamp(date.getTime());
	}	
	

	static private String[] getRemoteData(String line){
		String[] remote = new String[2];
		try{
		 String[] remoteArr = line.split("relay=")[1].split("\\]")[0].split("\\[");
		 remote[0] = remoteArr[0]; 
		 remote[1] = remoteArr[1];
		}catch(Exception e) {log.error(e.getMessage());}
		return remote;
	}
	
	static private String getIspResponse(String line){
		String response = "";
		try{
		  response = line.split("said: ")[1];
		  }catch(Exception e){log.error(e.getMessage());}
		return response;
	}
	
	
	
}
