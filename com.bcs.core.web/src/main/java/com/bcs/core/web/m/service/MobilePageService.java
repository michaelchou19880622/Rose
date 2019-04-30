package com.bcs.core.web.m.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.akka.service.AkkaCoreService;
import com.bcs.core.db.entity.UserTraceLog;
import com.bcs.core.db.service.UserTraceLogService;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.utils.ErrorRecord;

@Service
public class MobilePageService {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(MobilePageService.class);

	@Autowired
	private AkkaCoreService akkaCoreService;
	@Autowired
	private UserTraceLogService userTraceLogService;
	
	public void visitPageLog(String MID, String page, String msg){
		logger.debug("visitPageLog:" + MID + ":" + page + ":" + msg);

		try{
			if(StringUtils.isNotBlank(MID)){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date now = new Date();
				
				// ClickLink Log  
				UserTraceLog userTraceLog = new UserTraceLog();
				userTraceLog.setTarget(LOG_TARGET_ACTION_TYPE.TARGET_MobilePage);
				userTraceLog.setAction(LOG_TARGET_ACTION_TYPE.ACTION_VisitPage);
				userTraceLog.setModifyTime(now);
				userTraceLog.setModifyUser(MID);
				userTraceLog.setLevel(UserTraceLog.USER_TRACE_LOG_LEVEL_TRACE);
				userTraceLog.setModifyDay(sdf.format(now));
				
				userTraceLog.setContent(msg);
				userTraceLog.setReferenceId(page);
//				akkaCoreService.recordMsgs(userTraceLog);
				userTraceLogService.bulkPersist(userTraceLog);
			}
			else{
				logger.warn("visitPageLog:" + MID + ":" + page + ":" + msg);
			}
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
		}
	}
}
