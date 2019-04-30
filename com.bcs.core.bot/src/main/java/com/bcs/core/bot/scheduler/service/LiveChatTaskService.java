package com.bcs.core.bot.scheduler.service;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.springframework.stereotype.Service;

import org.quartz.CronTrigger;

import com.bcs.core.bot.scheduler.handler.CheckUserLiveChatStatus;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;

@Service
public class LiveChatTaskService {
	public void checkUserStatus() throws SchedulerException {
		SchedulerFactory sf = new StdSchedulerFactory();
	    Scheduler sched = sf.getScheduler();
	    String cronExpression = CoreConfigReader.getString(CONFIG_STR.LIVECHAT_STATUS_CHECK_CRON.toString());
		
		JobDetail jobDetail = newJob(CheckUserLiveChatStatus.class).withIdentity("checkUserStatus", "liveChat").build();
		CronTrigger trigger = newTrigger().withIdentity("trigger1", "group1").withSchedule(cronSchedule(cronExpression)).build();
		
		sched.scheduleJob(jobDetail, trigger);
		
		sched.start();
	}
}