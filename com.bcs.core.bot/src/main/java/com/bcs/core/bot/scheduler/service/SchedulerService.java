package com.bcs.core.bot.scheduler.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Service;

import com.bcs.core.bot.scheduler.handler.ExecuteSendMsgTask;
import com.bcs.core.bot.scheduler.handler.SendMessageJob;
import com.bcs.core.db.entity.MsgMain;
import com.bcs.core.db.entity.SystemLog;
import com.bcs.core.db.service.MsgMainService;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;

@Service
public class SchedulerService {
	private static final String MSG_SEND_GROUP = "MSG_SEND_GROUP";
	private static final String SCHEDULER_FLAG = "SCHEDULER_FLAG";

	@Autowired
	private MsgMainService msgMainService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(SchedulerService.class);

	private SchedulerFactory sfb = new StdSchedulerFactory();
	private Scheduler scheduler;
	
	private Map<String, JobKey> onSchedulerList = new HashMap<String, JobKey>();
	
	public SchedulerService() throws Exception{
		logger.info("Constructor SchedulerService");
		scheduler = sfb.getScheduler();
		scheduler.start();
	}
	
	/**
	 * Start Schedule
	 * @throws SchedulerException
	 */
	public void startSchedule() throws SchedulerException{
		synchronized (SCHEDULER_FLAG) {
			logger.info("startSchedule");
			scheduler.start();
			if(onSchedulerList == null){
				onSchedulerList = new HashMap<String, JobKey>();
			}
		}
	}
	
	/**
	 * Stop Schedule : Wait for Executing Jobs to Finish
	 * @throws SchedulerException
	 */
	@PreDestroy
	public void stopSchedule(){
		synchronized (SCHEDULER_FLAG) {
			logger.info("[DESTROY] SchedulerService cleaning up...");
			try{
				scheduler.shutdown(true);
				onSchedulerList.clear();
				scheduler = null;
				onSchedulerList = null;
			}
			catch(Throwable e){}

			System.gc();
			logger.info("[DESTROY] SchedulerService destroyed");
		}
	}
	
	private void checkSchedule(String action, Long msgId) throws SchedulerException{

		if(scheduler == null || scheduler.isShutdown()){
			restartSchedule();
			SystemLogUtil.saveLogError("Scheduler", action, "Scheduler Restart", msgId.toString());
		}
	}
	
	private void checkSchedule(String action, String message) throws SchedulerException{

		if(scheduler == null || scheduler.isShutdown()){
			restartSchedule();
			SystemLogUtil.saveLogError("Scheduler", action, "Scheduler Restart", message);
		}
	}
	
	private void restartSchedule(){
		logger.info("[RESTART] SchedulerService restartSchedule");
		try{
			scheduler = sfb.getScheduler();
			scheduler.start();
			onSchedulerList = new HashMap<String, JobKey>();
		}
		catch(Throwable e){}

		logger.info("[RESTART] SchedulerService restartSchedule success");
	}
	
	public void loadScheduleFromDB() throws Exception{
		logger.debug("loadScheduleFromDB");
		
		if(!CoreConfigReader.isMainSystem()){
			return;
		}
		
		List<MsgMain> list = msgMainService.findByStatus( MsgMain.MESSAGE_STATUS_SCHEDULED);
		for(MsgMain msgMain : list){
			String statusNotice = "";
			try{
				logger.debug("msgMain:" + msgMain);
				
				String sendType = msgMain.getSendType();
				logger.debug("sendType:" + sendType);
				if(MsgMain.SENDING_MSG_TYPE_DELAY.equals(sendType)){
					Date startTime = parseToDate(msgMain.getScheduleTime());
					logger.debug("startTime:" + startTime);
					
					// Overtime Skip
					if((new Date()).getTime() > startTime.getTime()){
						// Change Status to OverTime
						statusNotice = "設定預約發送超時";
						throw new Exception("OverTime");
					}
					else{
						// Schedule Send Message Delay
						addMsgSendSchedule(msgMain.getMsgId(), startTime);
						continue;
					}
				}
				else if(MsgMain.SENDING_MSG_TYPE_SCHEDULE.equals(sendType)){
					String cronExpression = parseToCronExpression(msgMain.getScheduleTime());
					logger.debug("cronExpression:" + cronExpression);
					
					addMsgSendSchedule(msgMain.getMsgId(), cronExpression);
					continue;
				}
				else{
					throw new Exception("Error SendType:" + sendType);
				}
			}
			catch(Exception e){
				logger.error(ErrorRecord.recordError(e));
				Map<String, Object> logContent = new HashMap<String, Object>();
				logContent.put("MsgMain", msgMain);
				logContent.put("ErrorMsg", e.getMessage());
				statusNotice = e.getMessage();
				
				this.saveLog(logContent, msgMain.getMsgId().toString());
			}
			
			// Schedule Send Message Fail, Include OverTime
			msgMain.setStatus(MsgMain.MESSAGE_STATUS_FAIL);
			msgMain.setStatusNotice(statusNotice);
			msgMain.setModifyTime(new Date());
			msgMainService.save(msgMain);
		}
	}
	
	private void saveLog(Map<String, Object> logContent, String referenceId){
		SystemLogUtil.saveLogError("ScheduleMsg", "LoadScheduleFromDB", SystemLog.SYSTEM_EVENT, logContent, referenceId);
	}
	
	public Date parseToDate(String sendingMsgTime) throws Exception{

		try{
			DateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
			return format.parse(sendingMsgTime);
		}
		catch(Exception e){
			throw new Exception("SendingMsgTime Parse Error:" + sendingMsgTime);
		}
	}
	
	public String parseToCronExpression(String sendingMsgTime) throws Exception{
		
		String result = "0 ";
		
		String[] split = sendingMsgTime.split(" ");
		String type = split[0];
		/**
		 * EveryMonth 11 12:13:00
		 */
		if(MsgMain.SENDING_MSG_SCHEDULED_TYPE_EVERY_MONTH.equals(type)){
			String day = split[1];
			String min = split[2].split(":")[1];
			String hour = split[2].split(":")[0];
			result = generateCronExpression("0", min, hour, day, "*", "?", "*");
		}
		/**
		 * EveryWeek 1 12:13:00
		 */
		else if(MsgMain.SENDING_MSG_SCHEDULED_TYPE_EVERY_WEEK.equals(type)){
			String week = split[1];
			String min = split[2].split(":")[1];
			String hour = split[2].split(":")[0];
			result = generateCronExpression("0", min, hour, "?", "*", week, "*");
		}
		/**
		 * EveryDay 12:13:00
		 */
		else if(MsgMain.SENDING_MSG_SCHEDULED_TYPE_EVERY_DAY.equals(type)){
			String min = split[1].split(":")[1];
			String hour = split[1].split(":")[0];
			result = generateCronExpression("0", min, hour, "*", "*", "?", "*");
		}
		logger.info("parseToCronExpression:" + result );
		
		return result;
	}
	
	private String generateCronExpression(String sec, String min, String hour, String day, String month, String week, String year){
		return sec + " " + min + " " + hour + " " + day + " " + month + " " + week + " " + year;
	}
	
	private JobDetail createJobDetail(Long msgId){

		String detailName = createDetailName(msgId);
		String detailGroup = MSG_SEND_GROUP; 
		
		/**
		 * Setting JobDetailFactoryBean
		 */
		JobDetailFactoryBean detailFactory = new JobDetailFactoryBean();
		detailFactory.setName(detailName);
		detailFactory.setGroup(detailGroup);
		detailFactory.setBeanName(detailName);
        
		detailFactory.setJobClass(SendMessageJob.class);
        
        Map<String, Object> jobDataAsMap = new HashMap<String, Object>();
        ExecuteSendMsgTask runTask = new ExecuteSendMsgTask();
        jobDataAsMap.put("sendMsgTask", runTask);
        jobDataAsMap.put("msgId", msgId);
        detailFactory.setJobDataAsMap(jobDataAsMap);
        
        /**
         * Create JobDetail
         */
        detailFactory.afterPropertiesSet();
        return (JobDetail) detailFactory.getObject();   
	}
	
	private CronTrigger createCronTrigger(String cronExpression, JobDetail jobDetail) throws Exception{

        /**
         * Setting CronTriggerFactoryBean
         */
        CronTriggerFactoryBean triggerFactory = new CronTriggerFactoryBean();
        triggerFactory.setName(UUID.randomUUID().toString());
        triggerFactory.setJobDetail(jobDetail);
        try {
        	triggerFactory.setCronExpression(cronExpression);
		} catch (Exception e) { // Handle
			logger.error(ErrorRecord.recordError(e));
			throw new Exception("CronExpression Error");
		}
        /**
         * Create CronTrigger
         */
        triggerFactory.afterPropertiesSet();
        
        return triggerFactory.getObject();
	}
	
	private Trigger createSimpleTrigger(Date startTime, JobDetail jobDetail) throws Exception{

        /**
         * Setting CronTriggerFactoryBean
         */
		SimpleTriggerFactoryBean triggerFactory = new SimpleTriggerFactoryBean();
        triggerFactory.setName(UUID.randomUUID().toString());
        triggerFactory.setJobDetail(jobDetail);
        try {
        	logger.debug("createSimpleTrigger:" + startTime);
        	triggerFactory.setStartTime(startTime);
        	triggerFactory.setRepeatCount(0);
        	triggerFactory.setRepeatInterval(1000);
		} catch (Exception e) { // Handle
			logger.error(ErrorRecord.recordError(e));
			throw new Exception("CronExpression Error");
		}
        /**
         * Create SimpleTrigger
         */
        triggerFactory.afterPropertiesSet();
        
        return triggerFactory.getObject();
	}
	
	/**
	 * Add Send Message Schedule
	 * @param command
	 * @return Trigger
	 * @throws Exception
	 */
	public void addMsgSendSchedule(Long msgId, Date startTime) throws Exception{
		logger.debug("addMsgSendSchedule:" + msgId);

		String detailName = createDetailName(msgId);

        /**
         * Create JobDetail
         */
        JobDetail jobDetail = createJobDetail(msgId);
        logger.debug(jobDetail);
       JobKey jobKey =  jobDetail.getKey();

       /**
        * Create SimpleTrigger
        */
       Trigger trigger = createSimpleTrigger(startTime, jobDetail);
        logger.debug(trigger);

    	try {
    		synchronized (SCHEDULER_FLAG) {
    			
    			checkSchedule("AddMsgSendSchedule", msgId);
    			
    			Date result = scheduler.scheduleJob(jobDetail, trigger);

    			logger.debug("addCommandSchedule result:" + result);
    			logger.debug("addCommandSchedule detailName:" + detailName);
    			onSchedulerList.put(detailName, jobKey);
    		}
		} catch (SchedulerException e) { // Handle
			logger.error(ErrorRecord.recordError(e));
			throw new Exception("Schedule Error");
		}
	}
	
	/**
	 * Add Send Message Schedule
	 * @param command
	 * @return Trigger
	 * @throws Exception
	 */
	public void addMsgSendSchedule(Long msgId, String cronExpression) throws Exception{
		logger.debug("addMsgSendSchedule:" + msgId);

		String detailName = createDetailName(msgId);

        /**
         * Create JobDetail
         */
        JobDetail jobDetail = createJobDetail(msgId);
        logger.debug(jobDetail);
       JobKey jobKey =  jobDetail.getKey();

       /**
        * Create CronTrigger
        */
       Trigger trigger = createCronTrigger(cronExpression, jobDetail);
        logger.debug(trigger);

    	try {
    		synchronized (SCHEDULER_FLAG) {

    			checkSchedule("AddMsgSendSchedule", msgId);
    			
    			Date result = scheduler.scheduleJob(jobDetail, trigger);

    			logger.debug("addCommandSchedule result:" + result);
    			logger.debug("addCommandSchedule detailName:" + detailName);
    			onSchedulerList.put(detailName, jobKey);
    		}
		} catch (SchedulerException e) { // Handle
			logger.error(ErrorRecord.recordError(e));
			throw new Exception("Schedule Error");
		}
	}
	
	/**
	 * Delete Send Message Schedule
	 * @param command
	 * @return true if the Job was found and deleted.
	 * @throws Exception
	 */
	public boolean deleteMsgSendSchedule(Long msgId) throws Exception{
		logger.info("deleteMsgSendSchedule:" + msgId);

		String detailName = createDetailName(msgId);

		if(msgId != null){
			logger.info("msgId : " + msgId);

			synchronized (SCHEDULER_FLAG) {

    			checkSchedule("DeleteMsgSendSchedule", msgId);
    			
				JobKey jobKey = onSchedulerList.get(detailName);
				if(jobKey != null){
					boolean result = scheduler.deleteJob(jobKey);
	
					logger.info("deleteMsgSendSchedule Success msgId:" + msgId);
					onSchedulerList.remove(detailName);
					
					return result;
				}
			}
		}
		
		return false;
	}
	
	private String createDetailName(Long msgId){
		return  "MsgId:" + msgId;
	}
	
	public void addScheduleEvent(String detailName, JobDetail jobDetail, Trigger trigger) throws Exception{
		logger.debug("addScheduleEvent:" + detailName);

        logger.debug(jobDetail);
       JobKey jobKey =  jobDetail.getKey();

       /**
        * Create SimpleTrigger
        */
        logger.debug(trigger);

    	try {
    		synchronized (SCHEDULER_FLAG) {
    			
    			checkSchedule("addScheduleEvent", detailName);
    			
    			Date result = scheduler.scheduleJob(jobDetail, trigger);

    			logger.debug("addScheduleEvent result:" + result);
    			logger.debug("addScheduleEvent detailName:" + detailName);
    			onSchedulerList.put(detailName, jobKey);
    		}
		} catch (SchedulerException e) { // Handle
			logger.error(ErrorRecord.recordError(e));
			throw new Exception("Schedule Error");
		}
	}
}
