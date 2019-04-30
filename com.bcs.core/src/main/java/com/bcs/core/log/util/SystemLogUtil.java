package com.bcs.core.log.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.bcs.core.db.entity.SystemLog;
import com.bcs.core.db.service.SystemLogService;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.record.service.CatchHandleMsgReceiveTimeout;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.DataSyncUtil;

public class SystemLogUtil {
	/** Logger */
	private static Logger logger = Logger.getLogger(SystemLogUtil.class);

	public static void timeCheck(String target, String action, Date start, int status, String log, String referenceId){
		timeCheck(target, action, start, status, 15000, log, referenceId);
	}
	
	public static void timeCheck(LOG_TARGET_ACTION_TYPE target, LOG_TARGET_ACTION_TYPE action, Date start, int status, String log, String referenceId){
		timeCheck(target, action, start, status, 15000, log, referenceId);
	}

	public static void timeCheck(LOG_TARGET_ACTION_TYPE target, LOG_TARGET_ACTION_TYPE action, Date start, int status, int limitTime, String log, String referenceId){
		timeCheck(target.toString(), action.toString(), start, status, limitTime, log, referenceId);
	}
	
	public static void timeCheck(String target, String action, Date start, int status, int limitTime, String log, String referenceId){
		Long diff = new Date().getTime() - start.getTime() ;
		logger.info(target + "-" + action + " Response Status:" + status + "-t-" + diff + "--" + referenceId);
		if(diff > limitTime){
			String str =target + "-" + action + "  Response Error Status:" + status + "-t-" + diff + "--" + log;
			logger.error(str);
			saveLogError(target, action, str, referenceId + "-t:" + diff);
			
			try{
				if(LOG_TARGET_ACTION_TYPE.ACTION_HandleMsgReceive.equals(LOG_TARGET_ACTION_TYPE.valueOf("ACTION_" + action))){
			    	ApplicationContextProvider.getApplicationContext().getBean(CatchHandleMsgReceiveTimeout.class).incrementCount();
				}
			}
			catch(Exception e){}
		}
	}

	public static void saveLogError(LOG_TARGET_ACTION_TYPE target, LOG_TARGET_ACTION_TYPE action, Object content, String referenceId){
		saveLogError(target.toString(), action.toString(), content, referenceId);
	}

	public static void saveLogError(LOG_TARGET_ACTION_TYPE target, LOG_TARGET_ACTION_TYPE action, String modifyUser, Object content, String referenceId){
		saveLogError(target.toString(), action.toString(), modifyUser, content, referenceId);
	}
	
	public static void saveLogError(String target, String action, Object content, String referenceId){
		saveLogError(target, action, "SYSTEM", content, referenceId);		
	}
	
	public static void saveLogError(String target, String action, String modifyUser, Object content, String referenceId){
		saveLogByLevel(target, action, modifyUser, content, referenceId, SystemLog.SYSTEM_LOG_LEVEL_ERROR);
	}
	
	public static void saveLogWarn(String target, String action, String user, Object content, String referenceId){
		saveLogByLevel(target, action, user, content, referenceId, SystemLog.SYSTEM_LOG_LEVEL_WARN);
	}
	
	public static void saveLogDebug(String target, String action, String modifyUser, Object content, String referenceId){
		saveLogByLevel(target, action, modifyUser, content, referenceId, SystemLog.SYSTEM_LOG_LEVEL_DEBUG);
	}
	
	public static void saveLogByLevel(String target, String action, String user, Object content, String referenceId, String level){

		try{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date now = new Date();
			
			SystemLog systemLog = new SystemLog();
			systemLog.setTarget(target);
			systemLog.setAction(action);
			systemLog.setModifyUser(user);
			systemLog.setModifyTime(now);
			systemLog.setLevel(level);
			systemLog.setModifyDay(sdf.format(now));
			systemLog.setSystemIp(DataSyncUtil.thisServerIp());
	
			systemLog.setContent(content);
			systemLog.setReferenceId(referenceId);
			ApplicationContextProvider.getApplicationContext().getBean(SystemLogService.class).bulkPersist(systemLog);
//	    	ApplicationContextProvider.getApplicationContext().getBean(AkkaCoreService.class).recordMsgs(systemLog);
		}
		catch(Exception e){
//			logger.error(ErrorRecord.recordError(e));
		}
	}
}
