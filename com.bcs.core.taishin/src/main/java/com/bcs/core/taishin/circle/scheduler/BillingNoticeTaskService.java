package com.bcs.core.taishin.circle.scheduler;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.service.BillingNoticeService;

@Service
public class BillingNoticeTaskService {
	@Autowired
	private BillingNoticeService billingNoticeService;
	
	/**
	 * 排程通知
	 * @param billingNoticeMain
	 *  @param scheduleTime
	 * @throws SchedulerException
	 */
	public void startTask(BillingNoticeMain billingNoticeMain, Date scheduleTime) throws SchedulerException {
		if (scheduleTime == null) {
			return;
		}
		SchedulerFactory schedulerFactory = new StdSchedulerFactory();
	    Scheduler scheduler = schedulerFactory.getScheduler();
	    
	    JobDetail jobDetail = newJob(BillingNoticeTask.class).withIdentity("billingNoticeMain", "BillingNotice").build();
	    Trigger trigger = newTrigger().withIdentity("billingNoticeMain", "BillingNotice").startAt(scheduleTime).build();
	    
	    scheduler.getContext().put("BillingNoticeMain", billingNoticeMain);
	    
	    scheduler.scheduleJob(jobDetail, trigger);
	    billingNoticeService.updateMainAndDetailStatus(billingNoticeMain, BillingNoticeMain.SENDING_MSG_TYPE_SCHEDULE);
	    scheduler.start();
	    
	}
}
