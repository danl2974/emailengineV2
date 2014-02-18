package com.lambdus.emailengine;

import java.math.BigInteger;

import sun.misc.BASE64Encoder;

import java.nio.charset.Charset;


public class MailingProperties {
	
	public String domain;
	
	public int templateId;
	
	public String recipient;
	
	public String base64Recipient;
	
	public String hexRecipient;
	
	public String uuid;
	
	//Constructor for Tx Mailing
	public MailingProperties(String emailAddress, int templateId){
		this.templateId = templateId;
		this.recipient = emailAddress;
		this.base64Recipient = base64(emailAddress);
		this.hexRecipient = hex(emailAddress);
		//Transactional Email Uuid
		this.uuid = "tx";
	}
	
	public MailingProperties(String emailAddress, int templateId, String uuid){
		this.templateId = templateId;
		this.recipient = emailAddress;
		this.base64Recipient = base64(emailAddress);
		this.hexRecipient = hex(emailAddress);
		this.uuid = uuid;
	}	
	
	public static String base64(String emailAddress){
		BASE64Encoder base64encoder = new BASE64Encoder();
		return base64encoder.encode(emailAddress.getBytes());
		
	}
	
	public static String hex(String emailAddress){
		return String.format("%040x", new BigInteger(1, emailAddress.getBytes(Charset.defaultCharset())));
	}

}
