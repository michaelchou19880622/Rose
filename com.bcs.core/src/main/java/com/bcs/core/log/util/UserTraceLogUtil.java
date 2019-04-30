package com.bcs.core.log.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.bcs.core.db.entity.UserTraceLog;
import com.bcs.core.db.service.UserTraceLogService;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.spring.ApplicationContextProvider;

public class UserTraceLogUtil {
	
	public static void saveLogTrace(LOG_TARGET_ACTION_TYPE target, LOG_TARGET_ACTION_TYPE action, String user, Object content, String referenceId){

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date now = new Date();
		
		UserTraceLog userTraceLog = new UserTraceLog();
		userTraceLog.setTarget(target);
		userTraceLog.setAction(action);
		userTraceLog.setModifyUser(user);
		userTraceLog.setModifyTime(now);
		userTraceLog.setLevel(UserTraceLog.USER_TRACE_LOG_LEVEL_TRACE);
		userTraceLog.setModifyDay(sdf.format(now));

		userTraceLog.setContent(content);
		userTraceLog.setReferenceId(referenceId);
		ApplicationContextProvider.getApplicationContext().getBean(UserTraceLogService.class).bulkPersist(userTraceLog);
//    	ApplicationContextProvider.getApplicationContext().getBean(AkkaCoreService.class).recordMsgs(userTraceLog);
	}
}
