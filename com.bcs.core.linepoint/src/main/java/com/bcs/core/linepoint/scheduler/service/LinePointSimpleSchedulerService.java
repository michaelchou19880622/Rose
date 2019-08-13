package com.bcs.core.linepoint.scheduler.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Service;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.scheduler.handler.LinePointTask;


@Service
public class LinePointSimpleSchedulerService {
	private static Logger logger = Logger.getLogger(LinePointSimpleSchedulerService.class);
	
	public void createLinePointTask(LinePointDetail linePointDetail, Date startTime) throws SchedulerException {
		try {		
			// Create JobDetail
			JobDetail jobDetail = JobBuilder.newJob(LinePointTask.class).build();

			// Create Trigger
			Trigger trigger = createSimpleTrigger(startTime, jobDetail);
			
			// Create Scheduler
			SchedulerFactory schedulerFactory = new StdSchedulerFactory();
		    Scheduler scheduler = schedulerFactory.getScheduler();
		    
			// Start Scheduler
		    scheduler.getContext().put("LinePointDetail", linePointDetail);
			scheduler.scheduleJob(jobDetail, trigger);
			scheduler.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private Trigger createSimpleTrigger(Date startTime, JobDetail jobDetail) throws Exception{

        // Setting CronTriggerFactoryBean
		SimpleTriggerFactoryBean triggerFactory = new SimpleTriggerFactoryBean();
        triggerFactory.setName(UUID.randomUUID().toString());
        triggerFactory.setJobDetail(jobDetail);
        try {
        	logger.info("createSimpleTrigger:" + startTime);
        	triggerFactory.setStartTime(startTime);
        	triggerFactory.setRepeatCount(0);
        	triggerFactory.setRepeatInterval(1000);
		} catch (Exception e) { // Handle
			logger.error(ErrorRecord.recordError(e));
			throw new Exception("CronExpression Error");
		}

        // Create SimpleTrigger
        triggerFactory.afterPropertiesSet();
        
        return triggerFactory.getObject();
	}
}
