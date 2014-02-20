package com.lambdus.emailengine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;

import com.lambdus.emailengine.persistence.EventDropLocal;
import com.lambdus.emailengine.persistence.TemplatePersist;
import com.lambdus.emailengine.webservices.RestDefinition;


@Stateless
@LocalBean
public class BatchQueueProducer {
        
    private static final Logger log = Logger.getLogger(BatchQueueProducer.class.getName());
        
    private ConnectionFactory connectionFactory;

    private Queue batchQueue;
    
    private HashMap<String,Object> batchData; 
    private BatchRequest request;
    
    private MessageProducer messageProducer;
    private Connection connection = null;
    private Session session;
    private Destination destination;
    
    
    public void initialize(HashMap<String,Object> batchData, BatchRequest request){
    	this.request = request;
    	this.batchData = batchData; 	
    }
      
    
    //DO MAIN STUFF HERE
    public int processBatch(){
    	 log.info("processBatch called");
    	 int totalProcessed = 0;
    	 TemplatePersist templateData = MessageAssembler.retrieveTemplateFromDB(this.request.getTemplateId());
    	 //Drop Campaign Detail
    	 EventDropLocal.addBatchCampaignDetail(templateData, this.request);
    	 
    	 log.info("target set size " + String.valueOf(batchData.size()) );
    	 startConnection();
    	
    	 for (Map.Entry<String, Object> recipientData : batchData.entrySet()) {
    		 
    		 String email = recipientData.getKey();
    		 HashMap<String,String> uniqueParams = (HashMap<String,String>) recipientData.getValue();
    		 log.info("processBatch - " + email);
    		 MailingProperties mprop = new MailingProperties(email, this.request.getTemplateId(), this.request.getUuid());
    		 mprop.domain = templateData.getDomain();
    		 String detokenedMessage = MessageAssembler.replaceTokens(templateData.getCreative(), uniqueParams);
    		 String assembledMessage = MessageAssembler.convertTrackingLinks(detokenedMessage, mprop);
    		 MapMessage jmsMessage = createJmsMessage(email, templateData, assembledMessage, mprop);
    		 try{
    		    this.messageProducer.send(jmsMessage);
    		    totalProcessed ++;
    		 }catch (JMSException jmse) {
    	        log.error(jmse.getMessage());
    	      }
    		 
    	 }
    	 
    	 /*
    	 Iterator<Map.Entry<String, Object>> it = batchData.entrySet().iterator();
    	 while (it.hasNext()) {
    		 Map.Entry recipientData = (Map.Entry) it.next();
    		 String email = (String) recipientData.getKey();
    		 HashMap<String,String> uniqueParams = (HashMap<String,String>) recipientData.getValue();
    		 log.info("processBatch - " + email);
    		 it.remove();
    		 
    		 String assembledMessage = MessageAssembler.replaceTokens(templateData.getCreative(), uniqueParams);
    		 MapMessage jmsMessage = createJmsMessage(email, templateData, assembledMessage);
    		 try{
    		    this.messageProducer.send(jmsMessage);
    		 }catch (JMSException jmse) {
    	        log.error(jmse.getMessage());
    	      }
    		 
    	 }
    	 */
    	 
    	 //CLEAN UP
    	 try{
            this.messageProducer.close();
            this.session.close();
            this.connection.close();
    	 }
    	 catch (JMSException jmsce) {
	        log.error(jmsce.getMessage());
	      }
    	
    	return totalProcessed;
    }
    
 
    public void startConnection()
    {
          log.info("jms startConnection called");
          try
          {
          InitialContext ctx = new InitialContext();
          connectionFactory = (ConnectionFactory) ctx.lookup("java:/ConnectionFactory");
          batchQueue = (Queue) ctx.lookup("java:/queue/batchemail");
          this.connection = this.connectionFactory.createConnection();
          this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
          this.destination = batchQueue;
          this.messageProducer = session.createProducer(this.destination);
          this.connection.start();
            }
            catch (JMSException e) {
                    log.info("jms exception");
            }
            catch (Exception ex) {
                    log.info("general exception");
            }
            
    }
    
    public MapMessage createJmsMessage(String email, TemplatePersist templateData, String message, MailingProperties mailingProperties)
    {
    	MapMessage mapMessage = null;	
            try
            {
              mapMessage = this.session.createMapMessage();	
              mapMessage.setString("emailAddress", email);
              mapMessage.setString("emailCreative", message);
              mapMessage.setString("subjectLine", templateData.getSubjectline());
              mapMessage.setString("fromAddress", templateData.getFromaddress());
              mapMessage.setString("fromName", templateData.getFromname());
              mapMessage.setInt("templateId", mailingProperties.templateId);
              mapMessage.setString("uuid", request.getUuid());
              
            }
            catch (JMSException e) {
              log.error(e.getMessage());      
            }
            
         return mapMessage;    
            
    }
        

}