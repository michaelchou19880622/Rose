package com.bcs.core.bot.scheduler.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Service;

import com.bcs.core.api.service.model.PushApiModel;
import com.bcs.core.bot.scheduler.handler.PushMessageTask;

@Service
public class PushMessageTaskService {
	public void startTask(PushApiModel pushApiModel) throws SchedulerException {
		SchedulerFactory schedulerFactory = new StdSchedulerFactory();
	    Scheduler scheduler = schedulerFactory.getScheduler();
	    
	    JobDetail jobDetail = newJob(PushMessageTask.class).withIdentity("pushApi", "PNP").build();
	    Trigger trigger = newTrigger().withIdentity("pushApi", "PNP").startAt(pushApiModel.getSendTimeSet()).build();
	    
	    scheduler.getContext().put("PushApiModel", pushApiModel);
	    
	    scheduler.scheduleJob(jobDetail, trigger);
	    scheduler.start();
	}
}