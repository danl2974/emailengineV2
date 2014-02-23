package com.lambdus.emailengine;

import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.jboss.logging.Logger;

public class SMTPClient {
        
        private static final Logger log = Logger.getLogger(SMTPClient.class.getName());
        
        private Properties properties = System.getProperties();
        private Session session;
        private String toAddress;
        private String fromAddress;
        private String fromName;
        private String subjectLine;
        private String emailCreative;
        private int templateId;
        private String uuid;
        private String canonical;
        
        public SMTPClient(String emailAddress, String emailCreative, String subjectLine, String fromAddress, String fromName) 
        {
        
        this.properties.setProperty("mail.smtp.host", "localhost");
        this.session = Session.getDefaultInstance(this.properties);
        this.toAddress = emailAddress;
        this.emailCreative = emailCreative;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.subjectLine = subjectLine;
        
        }
        
        public SMTPClient(String emailAddress, String emailCreative, String subjectLine, String fromAddress, String fromName, int templateId) 
        {
        this.properties.setProperty("mail.smtp.host", "localhost");
        this.properties.setProperty("mail.smtp.port", "587");
        this.session = Session.getInstance(this.properties);
        this.toAddress = emailAddress;
        this.emailCreative = emailCreative;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.subjectLine = subjectLine;
        this.templateId = templateId;
        
        }
        
        public SMTPClient(String emailAddress, String emailCreative, String subjectLine, String fromAddress, String fromName, int templateId, String uuid) 
        {
        //this.canonical = randomSelectCanonicalSender(fromAddress, 2);	
        this.properties.setProperty("mail.smtp.host", "localhost");
        this.properties.setProperty("mail.smtp.port", "587");
        //this.properties.setProperty("mail.smtp.from", this.canonical); //Return Path
        this.session = Session.getInstance(this.properties);
        this.toAddress = emailAddress;
        this.emailCreative = emailCreative;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.subjectLine = subjectLine;
        this.templateId = templateId;
        this.uuid = uuid;
        
        }        
        
        
        public void sendmail()
        {
        try{
          /*	
          MimeMessage message = new MimeMessage(this.session);
          message.setHeader("X-MailingID", String.format("%d", this.templateId));
          message.setHeader("X-FBL", MailingProperties.base64(this.toAddress));
          message.setFrom(new InternetAddress(this.fromAddress, this.fromName));
          message.addRecipient(Message.RecipientType.TO,
                                  new InternetAddress(this.toAddress));
          message.setSubject(this.subjectLine);
          message.setContent(this.emailCreative,
                                    "text/html");
          */
          CustomMimeMessage message = new CustomMimeMessage(this.session, this.templateId);
          message.setHeader("X-MailingID", String.format("%d::%s", this.templateId, this.uuid));
          message.setHeader("X-FBL", MailingProperties.base64(this.toAddress));
          //message.setFrom(new InternetAddress(this.fromAddress, this.fromName));
          
          //Change static 2 param to dynamic
          this.canonical = randomSelectCanonicalSender(this.fromAddress, 2);
          
          message.setFrom(new InternetAddress(this.canonical, this.fromName));
          //message.addFrom(new InternetAddress(this.canonical, this.fromName).getGroup(false));
          log.info("LOCAL " + this.canonical);
          
          message.addRecipient(Message.RecipientType.TO,
                                    new InternetAddress(this.toAddress));
          message.setSubject(this.subjectLine);
          message.setContent(this.emailCreative,
                                      "text/html; charset=\"UTF-8\"");
          message.setMessageIDDomain(this.fromAddress);
          
          Transport.send(message);
          
      }catch (MessagingException mex) {
              log.info(mex.getMessage());
      }catch (UnsupportedEncodingException uee) {
                 log.info(uee.getMessage());
        }
        }
        
        private String randomSelectCanonicalSender(String from, int hostlimit){
        	try{
        	String domain = from.split("\\@")[1];
        	String [] localhandles = new String[hostlimit];
        	for (int i = 0; i < hostlimit; i++){
        		localhandles[i] = ("local" + (i+1));
        	}
        	String randHandle = localhandles[new Random().nextInt(localhandles.length)];
        	return (randHandle + "@" + domain);
        	}catch(Exception e){log.error(e.getMessage()); return "";}
        }

}