package com.lambdus.emailengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.logging.Logger;

import com.lambdus.emailengine.persistence.TemplatePersist;
import com.lambdus.emailengine.persistence.SessionFactoryUtil;

import redis.clients.jedis.Jedis;


public class MessageAssembler {
        
        private static final Logger log = Logger.getLogger(MessageAssembler.class.getName());
        private static final String[] templateKeys = {"creative","subjectline","fromaddress", "fromname"};
        
        public String template;
        public String assembledMessage;
        public String subjectLine;
        public String fromAddress;
        public String fromName;
        
        public MessageAssembler(int templateId, HashMap<String, String> tokenKVPairs)
        {
                getTemplate(templateId);
                this.assembledMessage = replaceTokens(this.template, tokenKVPairs);
        }
        
        public void getTemplate(int templateId)
        {
                checkCache(templateId);
        }
        
        public String replaceTokens(String template, HashMap<String, String> tokenKVPairs)
        {
                Set<String> keySet = tokenKVPairs.keySet();
                
                for (String k : keySet){
                          String token = new StringBuilder().append("##").append(k).append("##").toString();
                          template = template.replaceAll(token, tokenKVPairs.get(k));
                        }
                
                return template;
                
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
               addRedisCache(templateId, templateData);
        	  }
       	      catch(Exception e){
    		   log.error(e.getMessage());
    	      }        	
           }
             
             
        }
        
        private TemplatePersist retrieveTemplateFromDB(int templateId)
        {
          TemplatePersist result = null;
          try{
             Session session = SessionFactoryUtil.getSessionFactory().getCurrentSession();
             session.getTransaction().begin();
             Criteria cb = session.createCriteria(TemplatePersist.class);
             result = (TemplatePersist) cb.add(Restrictions.eq("id",templateId)).uniqueResult();
             session.getTransaction().commit();
             
             log.info("Template ID " + result.getId() + "fetched from Database");
             
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
               jedis.hmset(String.valueOf(templateId), redisHashMap);
               jedis.expire(String.valueOf(templateId), 60);
               }
            catch (Exception e){
               log.error(e.getMessage());
            }
          
        }
}