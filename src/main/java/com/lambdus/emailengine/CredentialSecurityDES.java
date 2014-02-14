package com.lambdus.emailengine;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.jboss.logging.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CredentialSecurityDES {
	
   private static final Logger log = Logger.getLogger(CredentialSecurityDES.class.getName());	

   private static final String UNICODE_FORMAT = "UTF8";
   public static final String DES_ENCRYPTION_SCHEME = "DES";
   private KeySpec myKeySpec;
   private SecretKeyFactory mySecretKeyFactory;
   private Cipher cipher;
   byte[] keyAsBytes;
   private String myEncryptionKey;
   private String myEncryptionScheme;
   SecretKey key;

   public CredentialSecurityDES() throws Exception
   {
       myEncryptionKey = "EmailEngineSecretEncryptionKey";
       myEncryptionScheme = DES_ENCRYPTION_SCHEME;
       keyAsBytes = myEncryptionKey.getBytes(UNICODE_FORMAT);
       myKeySpec = new DESKeySpec(keyAsBytes);
       mySecretKeyFactory = SecretKeyFactory.getInstance(myEncryptionScheme);
       cipher = Cipher.getInstance(myEncryptionScheme);
       key = mySecretKeyFactory.generateSecret(myKeySpec);
   }


   public String encrypt(String unencryptedString) {
       String encryptedString = null;
       try {
           cipher.init(Cipher.ENCRYPT_MODE, key);
           byte[] plainText = unencryptedString.getBytes(UNICODE_FORMAT);
           byte[] encryptedText = cipher.doFinal(plainText);
           BASE64Encoder base64encoder = new BASE64Encoder();
           encryptedString = base64encoder.encode(encryptedText);
       } catch (Exception e) {
           log.error(e.getMessage());
       }
       return encryptedString;
   }

   public String decrypt(String encryptedString) {
       String decryptedText=null;
       try {
           cipher.init(Cipher.DECRYPT_MODE, key);
           BASE64Decoder base64decoder = new BASE64Decoder();
           byte[] encryptedText = base64decoder.decodeBuffer(encryptedString);
           byte[] plainText = cipher.doFinal(encryptedText);
           decryptedText= bytes2String(plainText);
       } catch (Exception e) {
    	   log.error(e.getMessage());
       }
       return decryptedText;
   }

   private static String bytes2String(byte[] bytes) {
       StringBuffer stringBuffer = new StringBuffer();
       for (int i = 0; i < bytes.length; i++) {
           stringBuffer.append((char) bytes[i]);
       }
       return stringBuffer.toString();
   }


}