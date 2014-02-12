package com.lambdus.emailengine;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;



public class CustomMimeMessage extends MimeMessage {
	
	private String messageId = "mailer";
	private String domain;
	
	public CustomMimeMessage(Session session) {
	    super(session);
	    this.session=session;
	}
	
	@Override
    protected void updateMessageID() throws MessagingException {
	setHeader("Message-ID", "<" + String.valueOf(System.currentTimeMillis()) + ".Mailer@" + this.domain + ">");
    }
	
	public void setCustomMessageId(String messageId){
		this.messageId = messageId;
	}
	
	public void setMessageIDDomain(String domain){
		this.domain = domain.split("@")[1];
	}
	

}
