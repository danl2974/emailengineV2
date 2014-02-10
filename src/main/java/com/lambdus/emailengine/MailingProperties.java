package com.lambdus.emailengine;

import java.math.BigInteger;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.Charset;



public class MailingProperties {
	
	public String domain;
	
	public int templateId;
	
	public String recipient;
	
	public String base64Recipient;
	
	public String hexRecipient;
	
	public MailingProperties(String emailAddress, int templateId){
		this.templateId = templateId;
		this.recipient = emailAddress;
		this.base64Recipient = base64(emailAddress);
		this.hexRecipient = hex(emailAddress);
		
	}
	
	public static String base64(String emailAddress){
		
		byte[] b64bytes = Base64.encodeBase64(emailAddress.getBytes());
		return new String(b64bytes);
		
	}
	
	public static String hex(String emailAddress){
		return String.format("%040x", new BigInteger(1, emailAddress.getBytes(Charset.defaultCharset())));
	}

}
