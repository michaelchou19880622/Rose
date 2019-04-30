package com.bcs.web.ui.service;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.BusinessGroup;
import com.bcs.core.db.service.BusinessGroupService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;

@Service
public class BusinessGroupUIService {

	/** Logger */
	private static Logger logger = Logger.getLogger(BusinessGroupUIService.class);
	
	@Autowired
	private BusinessGroupService businessGroupService;
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
    public void deleteFromUI(String groupId, String account) throws BcsNoticeException {
        logger.info("deleteFromUI:" + groupId);
        
        BusinessGroup businessGroup = businessGroupService.findOne(groupId);
        businessGroupService.delete(groupId);
        String groupName = "Unknow";
        if(businessGroup != null) {
            groupName = businessGroup.getGroupName();
        }
        createSystemLog("Delete", groupName, account, new Date(), groupId.toString());
    }
	
	private void createSystemLog(String action, Object content, String modifyUser, Date modifyTime, String referenceId) {
        SystemLogUtil.saveLogDebug("Business", action, modifyUser, content, referenceId);
    }
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
    public BusinessGroup saveFromUI(BusinessGroup businessGroup, String account) throws BcsNoticeException{
        logger.info("saveFromUI:" + businessGroup);

        String groupId = businessGroup.getGroupId();
        
        BusinessGroup businessGroupInDb = businessGroupService.findByName(businessGroup.getGroupName());
        
        if (businessGroupInDb == null) {
            String action = "Edit";
            
            if (groupId == null) {
                action = "Create";
                
                groupId = businessGroupService.generateGroupId(); 
                businessGroup.setGroupId(groupId);
            }
            
            // Set Modify Admin User
            businessGroup.setModifyUser(account);
            businessGroup.setModifyTime(new Date());
            
            // Save BusinessGroup
            businessGroupService.save(businessGroup);
            
            businessGroup = businessGroupService.findOne(businessGroup.getGroupId());
            createSystemLog(action, businessGroup, businessGroup.getModifyUser(), businessGroup.getModifyTime(), businessGroup.getGroupId().toString());
            return businessGroup;
        } else {
            logger.debug("groupName:" + businessGroup.getGroupName() + " have already exist.");
            
            return businessGroupInDb;
        }
    }
}
