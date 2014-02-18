package com.lambdus.emailengine;


public class BatchRequest {

    int targetId = 0;
    int templateId = 0;
    String uuid = "none";
    
    public void setTargetId(int targetId)
    {
            this.targetId = targetId;
    }
    
    public void setTemplateId(int templateId)
    {
            this.templateId = templateId;
    }
    
    public void setUuid(String uuid)
    {
            this.uuid = uuid;
    }    
    
    public int getTargetId()
    {
            return this.targetId;
    }
    
    public int getTemplateId()
    {
            return this.templateId;
    }
    
    public String getUuid()
    {
            return this.uuid;
    } 
    
    	
	
	
}
