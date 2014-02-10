package com.lambdus.emailengine;

import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MapMessage;


     @MessageDriven(name = "BatchMailQueue", activationConfig = {
     @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
     @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/batchemail"),
     @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
      public class BatchQueueConsumer implements MessageListener {
        
      private final static Logger LOGGER = Logger.getLogger(BatchQueueConsumer.class.toString());
      
 
      public void onMessage(Message rcvMessage) {
          MapMessage mapMessage = null;
          try {
              if (rcvMessage instanceof MapMessage) {
                  mapMessage = (MapMessage) rcvMessage;
                  
                  SMTPClient smtpc = new SMTPClient(
                  mapMessage.getString("emailAddress"),
                  mapMessage.getString("emailCreative"),
                  mapMessage.getString("subjectLine"),
                  mapMessage.getString("fromAddress"),
                  mapMessage.getString("fromName"),
                  mapMessage.getInt("templateId")
                  );
                  
                  try
                  {
                    smtpc.sendmail();
                  }
                  catch (Exception e) 
                  {
                	  LOGGER.warning(e.getMessage()); 
                  }
                  
                 
              } else {
                  LOGGER.warning("Message of wrong type: " + rcvMessage.getClass().getName());
              }
          }
          
          catch (JMSException e) {
              throw new RuntimeException(e);
          }
          catch (Exception ex){
                   LOGGER.info(ex.getMessage());
          }
          
      } 

}
