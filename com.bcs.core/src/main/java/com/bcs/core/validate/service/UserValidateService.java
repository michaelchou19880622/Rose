package com.bcs.core.validate.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.utils.ErrorRecord;

@Service
public class UserValidateService {
	@Autowired
	private LineUserService lineUserService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(UserValidateService.class);
	
	public boolean isBinding(String MID){
		
		try{
			if(StringUtils.isNotBlank(MID)){
				LineUser lineUser = lineUserService.findByMid(MID);
				// Validate MID is Binding
				if(lineUser != null && LineUser.STATUS_BINDED.equals(lineUser.getStatus())){
					return true;
				}
			}
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
		}
		
		return false; 
	}
}
