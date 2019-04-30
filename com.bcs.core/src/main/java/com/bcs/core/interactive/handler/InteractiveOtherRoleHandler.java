package com.bcs.core.interactive.handler;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.service.GroupGenerateService;
import com.bcs.core.db.service.SendGroupService;
import com.bcs.core.utils.ErrorRecord;

@Service
public class InteractiveOtherRoleHandler {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(InteractiveOtherRoleHandler.class);

	@Autowired
	private GroupGenerateService groupGenerateService;
	@Autowired
	private SendGroupService sendGroupService;
	
	public  boolean checkMatchOtherRole(String MID, String otherRole){
		if(StringUtils.isNotBlank(otherRole) && otherRole.startsWith("GROUPID")){
			try{
				String groupIdStr = otherRole.substring("GROUPID".length());
				Long groupId = Long.parseLong(groupIdStr);
				
				if(groupId > 0){
					return groupGenerateService.checkMIDBySendGroupDetailGroupId(groupId, MID);
				}
				else{
					return sendGroupService.checkMidExistDefaultGroup(groupId, MID);
				}
			}
			catch(Exception e){
	    		logger.error(ErrorRecord.recordError(e));
			}
		}
		
		return false;
	}
}
