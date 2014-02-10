package com.lambdus.emailengine;

import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.util.Properties;
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
        this.session = Session.getDefaultInstance(this.properties);
        this.toAddress = emailAddress;
        this.emailCreative = emailCreative;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.subjectLine = subjectLine;
        this.templateId = templateId;
        
        }
        
        
        public void sendmail()
        {
        try{
          MimeMessage message = new MimeMessage(this.session);
          message.setHeader("X-MailingID", String.format("%d", this.templateId));
          message.setHeader("X-FBL", MailingProperties.base64(this.toAddress));
          message.setFrom(new InternetAddress(this.fromAddress, this.fromName));
          message.addRecipient(Message.RecipientType.TO,
                                  new InternetAddress(this.toAddress));
          message.setSubject(this.subjectLine);
          message.setContent(this.emailCreative,
                                    "text/html");
          Transport.send(message);
          
      }catch (MessagingException mex) {
              log.info(mex.getMessage());
      }catch (UnsupportedEncodingException uee) {
                 log.info(uee.getMessage());
        }
        }

}