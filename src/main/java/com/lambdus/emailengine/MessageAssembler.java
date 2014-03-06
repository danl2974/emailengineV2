package com.lambdus.emailengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.logging.Logger;

import com.lambdus.emailengine.persistence.TemplatePersist;
import com.lambdus.emailengine.persistence.SessionFactoryUtil;

import redis.clients.jedis.Jedis;


public class MessageAssembler {
        
        private static final Logger log = Logger.getLogger(MessageAssembler.class.getName());
        private static final String[] templateKeys = {"creative","subjectline","fromaddress", "fromname", "domain"};
        
        public String template;
        public String assembledMessage;
        public String subjectLine;
        public String fromAddress;
        public String fromName;
        public String domain;
        
        public MessageAssembler(int templateId, HashMap<String, String> tokenKVPairs)
        {
                getTemplate(templateId);
                this.assembledMessage = replaceTokens(this.template, tokenKVPairs);
        }
        
        public MessageAssembler(int templateId, HashMap<String, String> tokenKVPairs, MailingProperties mailingProperties)
        {
                getTemplate(templateId);
                //resolve domain dependent on getTemplate
                mailingProperties.domain = this.domain;
                this.assembledMessage = convertTrackingLinks(replaceTokens(this.template, tokenKVPairs), mailingProperties);
        }        
        
        public void getTemplate(int templateId)
        {
                checkCache(templateId);
        }
        
        //CHANGED from public to public static
        public static String replaceTokens(String template, HashMap<String, String> tokenKVPairs)
        {
                Set<String> keySet = tokenKVPairs.keySet();
                
                for (String k : keySet){
                          String token = new StringBuilder().append("##").append(k).append("##").toString();
                          template = template.replaceAll(token, tokenKVPairs.get(k));
                        }
                
                return template;
                
        }
        
        
        public static String convertTrackingLinks(String text, MailingProperties mailingProperties)
        {
            String regex = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(text);
            
            String clickUrl = String.format("http://c.%s/track?type=click&t=%d&r=%s&u=%s", mailingProperties.domain, mailingProperties.templateId, mailingProperties.hexRecipient, mailingProperties.uuid);
            String openUrl = String.format("http://c.%s/track?type=open&t=%d&r=%s&u=%s", mailingProperties.domain, mailingProperties.templateId, mailingProperties.hexRecipient, mailingProperties.uuid);
            StringBuffer sb = new StringBuffer();
            log.info(clickUrl);

            while(m.find())
            {
               String found = m.group(0);
               String hyperlink = deriveHrefUrl(found);
               m.appendReplacement(sb, " href=\"" + clickUrl + "&&&" + hyperlink + "\"");
            }
            m.appendTail(sb);
            String trackedMessage;
            if (sb.toString().indexOf("</body>") != -1)
            {
            	trackedMessage = sb.toString().replace("</body>", "<img src=\"" + openUrl + "\" width=\"1\" height=\"1\" style=\"border-width:0; border-style:hidden;\" Alt=\"\" /></body>");
            }
            else if (sb.toString().indexOf("</html>") != -1)
            {
            	trackedMessage = sb.toString().replace("</html>", "<img src=\"" + openUrl + "\" width=\"1\" height=\"1\" style=\"border-width:0; border-style:hidden;\" Alt=\"\" /></html>");
            }            
            else
            {
            	trackedMessage = sb.append("<img src=\"" + openUrl + "\" width=\"1\" height=\"1\" style=\"border-width:0; border-style:hidden;\" Alt=\"\" />").toString();
            }
            
            return trackedMessage;
         }
        
        private void checkCache(int templateId)
        {
           //Redis in-memory cache key-value store - Jedis (Java Client)        
           Jedis jedis = new Jedis("localhost");
           
           if (jedis.exists(String.valueOf(templateId)))
           {
        	try{
               log.info("Exists in Redis");
               String[] templateVarargs = templateKeys;
               List<String> templateVals  = jedis.hmget(String.valueOf(templateId), templateVarargs);
               this.template = templateVals.get(0);
               this.subjectLine = templateVals.get(1);
               this.fromAddress = templateVals.get(2);
               this.fromName = templateVals.get(3);
               this.domain = templateVals.get(4);
        	 }
        	 catch(Exception e){
        		 log.error(e.getMessage());
        	 }
        	
           }
           else
           {
        	try{
               log.info("Fetch Template From Database");
               TemplatePersist templateData = retrieveTemplateFromDB(templateId);
               this.template = templateData.getCreative();
               this.subjectLine = templateData.getSubjectline();
               this.fromAddress = templateData.getFromaddress();
               this.fromName = templateData.getFromname();
               this.domain = templateData.getDomain();
               addRedisCache(templateId, templateData);
        	  }
       	      catch(Exception e){
    		   log.error(e.getMessage());
    	      }        	
           }
             
             
        }
        
        //CHANGED from private to public static
        public static TemplatePersist retrieveTemplateFromDB(int templateId)
        {
          TemplatePersist result = null;
          try{
             Session session = SessionFactoryUtil.getSessionFactory().getCurrentSession();
             session.getTransaction().begin();
             Criteria cb = session.createCriteria(TemplatePersist.class);
             result = (TemplatePersist) cb.add(Restrictions.eq("id",templateId)).uniqueResult();
             session.getTransaction().commit();
             
        	 }
           catch(Exception e){
        	 log.error(e.getMessage());
           }
          
          return result;
             
             
        }

        public String getAssembledMessage()
        {
                return this.assembledMessage;
        }

        public String getSubjectLine()
        {
                return this.subjectLine;
        }

        public String getFromAddress()
        {
                return this.fromAddress;
        }
        
        public String getFromName()
        {
                return this.fromName;
        }
        
        /*
        private ArrayList<String> collectTokenItems(HashMap<String, String> params)
        {
                Set<String> keySet = params.keySet();
                ArrayList<String> tokenList = new ArrayList<String>();
                for (String k : keySet){
                  String token = new StringBuilder().append("##").append(k).append("##").toString();
                  tokenList.add(token);
                }
                
                return tokenList;
        }
        */
        
        private void addRedisCache(int templateId, TemplatePersist templateData)
        {
            try{	
               Jedis jedis = new Jedis("localhost");
               HashMap<String, String> redisHashMap = new HashMap<String, String>();
               redisHashMap.put("creative", templateData.getCreative());
               redisHashMap.put("subjectline", templateData.getSubjectline());
               redisHashMap.put("fromaddress", templateData.getFromaddress());
               redisHashMap.put("fromname", templateData.getFromname());
               redisHashMap.put("domain", templateData.getDomain());
               jedis.hmset(String.valueOf(templateId), redisHashMap);
               jedis.expire(String.valueOf(templateId), 60);
               }
            catch (Exception e){
               log.error(e.getMessage());
            }
          
        }
        
        // extract hyperlink from href expression
        private static String deriveHrefUrl(String href)
        {
        	String[] parts = href.split("\"");
        	return parts[1];
        }
}