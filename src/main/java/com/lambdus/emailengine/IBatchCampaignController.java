package com.lambdus.emailengine;

import java.util.List;

public interface IBatchCampaignController {
	
	void startCampaign();
	
    void setTargetId(int targetId);

    void setTemplateId(int templateId);

    void setTargetIds(List<Integer> targetIds);
    
}
